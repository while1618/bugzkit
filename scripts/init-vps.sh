#!/usr/bin/env bash
# ==============================================================================
#  Bugzkit — Production VPS Initialization Script
#  Target : Debian 12 (Bookworm) on DigitalOcean
#  Run as : root
#
#  What this script does (in order):
#    1.  System update & essential packages
#    2.  Timezone → UTC + NTP
#    3.  Create non-root deploy user (key-only SSH)
#    4.  SSH hardening
#    5.  UFW firewall  (22 + 80 + 443)
#    6.  Fail2ban
#    7.  Automatic security updates (unattended-upgrades)
#    8.  Kernel hardening (sysctl)
#    9.  Swap file (2 GB)
#   10.  Docker CE
#   11.  Docker daemon — log rotation & security defaults
#   12.  Secret files  (mounted into containers via compose secrets)
#   13.  Deploy the stack
# ==============================================================================
set -euo pipefail

# ── colour helpers ─────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
success() { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
die()     { echo -e "${RED}[ERROR]${NC} $*" >&2; exit 1; }

[[ $EUID -ne 0 ]] && die "Run this script as root  (e.g. sudo bash init-vps.sh)"

apt_install() { DEBIAN_FRONTEND=noninteractive apt-get install -y -qq "$@"; }

# ==============================================================================
# 0. COLLECT INPUT
# ==============================================================================
echo ""
echo "========================================"
echo "  Bugzkit — Production VPS Init"
echo "========================================"
echo ""

read -rp "Deploy username (will be created): " DEPLOY_USER
[[ -z "$DEPLOY_USER" ]] && die "Username cannot be empty."

read -rp "SSH public key for $DEPLOY_USER: " SSH_PUB_KEY
[[ -z "$SSH_PUB_KEY" ]] && die "SSH public key cannot be empty."

echo ""
echo "--- Stack configuration (Enter to keep default) ---"
read -rp "  ACME / Let's Encrypt email [office@bugzkit.com]: " ACME_EMAIL
ACME_EMAIL="${ACME_EMAIL:-office@bugzkit.com}"

echo ""
echo "--- Secrets (input is hidden) ---"

read -rsp "  postgres_password    (≥16 chars): " POSTGRES_PASSWORD; echo
[[ ${#POSTGRES_PASSWORD} -lt 16 ]] && die "postgres_password must be ≥ 16 characters."

read -rsp "  redis_password       (≥16 chars): " REDIS_PASSWORD; echo
[[ ${#REDIS_PASSWORD} -lt 16 ]]    && die "redis_password must be ≥ 16 characters."

read -rsp "  jwt_secret           (≥32 chars): " JWT_SECRET; echo
[[ ${#JWT_SECRET} -lt 32 ]]        && die "jwt_secret must be ≥ 32 characters."

read -rsp "  smtp_password                   : " SMTP_PASSWORD; echo
read -rsp "  user_password                   : " USER_PASSWORD; echo

read -rsp "  google_client_id                : " GOOGLE_CLIENT_ID; echo
read -rsp "  google_client_secret            : " GOOGLE_CLIENT_SECRET; echo

echo ""
info "Starting setup — this may take a few minutes…"
echo ""

# ==============================================================================
# 1. SYSTEM UPDATE & PACKAGES
# ==============================================================================
info "Updating system packages…"
apt-get update -qq
DEBIAN_FRONTEND=noninteractive apt-get upgrade -y -qq
apt_install \
  curl wget gnupg2 ca-certificates lsb-release \
  ufw fail2ban unattended-upgrades apt-listchanges \
  logrotate chrony htop
success "System updated."

# ==============================================================================
# 2. TIMEZONE → UTC + NTP
# ==============================================================================
timedatectl set-timezone UTC
systemctl enable chrony --now &>/dev/null
success "Timezone: UTC, NTP: chrony."

# ==============================================================================
# 3. CREATE DEPLOY USER
# ==============================================================================
if id "$DEPLOY_USER" &>/dev/null; then
  warn "User '$DEPLOY_USER' already exists — skipping creation."
else
  useradd -m -s /bin/bash -G sudo "$DEPLOY_USER"
  passwd -l "$DEPLOY_USER"   # lock password → key-only login
  success "User '$DEPLOY_USER' created."
fi

SSH_DIR="/home/$DEPLOY_USER/.ssh"
mkdir -p "$SSH_DIR"
echo "$SSH_PUB_KEY" >> "$SSH_DIR/authorized_keys"
sort -u "$SSH_DIR/authorized_keys" -o "$SSH_DIR/authorized_keys"
chmod 700 "$SSH_DIR"
chmod 600 "$SSH_DIR/authorized_keys"
chown -R "$DEPLOY_USER:$DEPLOY_USER" "$SSH_DIR"
success "SSH key installed for '$DEPLOY_USER'."

# ==============================================================================
# 4. SSH HARDENING
# ==============================================================================
SSHD_CFG="/etc/ssh/sshd_config"
cp "$SSHD_CFG" "${SSHD_CFG}.bak.$(date +%s)"

ssh_set() {
  local key="$1" val="$2"
  if grep -qE "^#?${key}" "$SSHD_CFG"; then
    sed -i -E "s|^#?${key}.*|${key} ${val}|" "$SSHD_CFG"
  else
    echo "${key} ${val}" >> "$SSHD_CFG"
  fi
}

ssh_set PermitRootLogin              no
ssh_set PasswordAuthentication       no
ssh_set PubkeyAuthentication         yes
ssh_set AuthorizedKeysFile           ".ssh/authorized_keys"
ssh_set PermitEmptyPasswords         no
ssh_set KbdInteractiveAuthentication no
ssh_set X11Forwarding                no
ssh_set MaxAuthTries                 3
ssh_set LoginGraceTime               30
ssh_set AllowUsers                   "$DEPLOY_USER"
ssh_set ClientAliveInterval          300
ssh_set ClientAliveCountMax          2
ssh_set UseDNS                       no

systemctl reload sshd
success "SSH hardened (root login disabled, key-only, AllowUsers=$DEPLOY_USER)."

# ==============================================================================
# 5. UFW FIREWALL
# ==============================================================================
ufw --force reset  &>/dev/null
ufw default deny incoming
ufw default allow outgoing
ufw allow ssh
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable &>/dev/null
success "UFW: deny-in by default, allowed 22/80/443."

# ==============================================================================
# 6. FAIL2BAN
# ==============================================================================
cat > /etc/fail2ban/jail.local << 'EOF'
[DEFAULT]
bantime  = 1h
findtime = 10m
maxretry = 5
backend  = systemd

[sshd]
enabled  = true
port     = ssh
maxretry = 3
bantime  = 24h
EOF

systemctl enable fail2ban --now &>/dev/null
systemctl restart fail2ban
success "Fail2ban: SSH jail active (3 retries → 24h ban)."

# ==============================================================================
# 7. AUTOMATIC SECURITY UPDATES
# ==============================================================================
cat > /etc/apt/apt.conf.d/20auto-upgrades << 'EOF'
APT::Periodic::Update-Package-Lists "1";
APT::Periodic::Unattended-Upgrade "1";
APT::Periodic::AutocleanInterval "7";
EOF

cat > /etc/apt/apt.conf.d/50unattended-upgrades << 'EOF'
Unattended-Upgrade::Allowed-Origins {
  "${distro_id}:${distro_codename}-security";
};
Unattended-Upgrade::AutoFixInterruptedDpkg "true";
Unattended-Upgrade::MinimalSteps "true";
Unattended-Upgrade::Remove-Unused-Dependencies "true";
Unattended-Upgrade::Automatic-Reboot "false";
EOF

systemctl enable unattended-upgrades --now &>/dev/null
success "Automatic security updates enabled (security repos only, no auto-reboot)."

# ==============================================================================
# 8. KERNEL HARDENING (sysctl)
# ==============================================================================
cat > /etc/sysctl.d/99-hardening.conf << 'EOF'
# ── Reverse-path filter (IP spoofing) ─────────────────────────────────────────
net.ipv4.conf.all.rp_filter = 1
net.ipv4.conf.default.rp_filter = 1

# ── Ignore ICMP broadcasts & redirects ────────────────────────────────────────
net.ipv4.icmp_echo_ignore_broadcasts = 1
net.ipv4.conf.all.accept_redirects = 0
net.ipv4.conf.default.accept_redirects = 0
net.ipv4.conf.all.send_redirects = 0
net.ipv4.conf.default.send_redirects = 0

# ── SYN flood protection ───────────────────────────────────────────────────────
net.ipv4.tcp_syncookies = 1
net.ipv4.tcp_max_syn_backlog = 2048
net.ipv4.tcp_synack_retries = 2
net.ipv4.tcp_syn_retries = 5

# ── Block source routing ───────────────────────────────────────────────────────
net.ipv4.conf.all.accept_source_route = 0
net.ipv4.conf.default.accept_source_route = 0

# ── Log suspicious packets ─────────────────────────────────────────────────────
net.ipv4.conf.all.log_martians = 1

# ── Swappiness (prefer RAM, only swap under pressure) ─────────────────────────
vm.swappiness = 10

# ── Required by Redis ──────────────────────────────────────────────────────────
vm.overcommit_memory = 1

# ── File descriptors (Docker + Spring Boot) ────────────────────────────────────
fs.file-max = 100000
EOF

sysctl --system -q
success "Kernel hardening applied."

# Disable transparent huge pages (Redis recommendation)
if [[ -f /sys/kernel/mm/transparent_hugepage/enabled ]]; then
  echo never > /sys/kernel/mm/transparent_hugepage/enabled
  echo never > /sys/kernel/mm/transparent_hugepage/defrag
  cat > /etc/rc.local << 'EOF'
#!/bin/sh -e
echo never > /sys/kernel/mm/transparent_hugepage/enabled
echo never > /sys/kernel/mm/transparent_hugepage/defrag
exit 0
EOF
  chmod +x /etc/rc.local
  success "Transparent huge pages disabled (Redis)."
fi

# ==============================================================================
# 9. SWAP (2 GB)
# ==============================================================================
if swapon --show | grep -q /swapfile; then
  warn "Swapfile already active — skipping."
else
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap -q /swapfile
  swapon /swapfile
  grep -q '/swapfile' /etc/fstab || echo '/swapfile none swap sw 0 0' >> /etc/fstab
  success "2 GB swap created."
fi

# ==============================================================================
# 10. DOCKER CE
# ==============================================================================
if command -v docker &>/dev/null; then
  warn "Docker already installed: $(docker --version | cut -d, -f1)"
else
  info "Installing Docker CE…"
  install -m 0755 -d /etc/apt/keyrings
  curl -fsSL https://download.docker.com/linux/debian/gpg \
    | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
  chmod a+r /etc/apt/keyrings/docker.gpg
  echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/debian \
    $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
    > /etc/apt/sources.list.d/docker.list
  apt-get update -qq
  apt_install docker-ce docker-ce-cli containerd.io docker-compose-plugin
  systemctl enable docker --now &>/dev/null
  success "Docker CE installed."
fi

usermod -aG docker "$DEPLOY_USER"
success "'$DEPLOY_USER' added to docker group."

# ==============================================================================
# 11. DOCKER DAEMON — log rotation & security defaults
# ==============================================================================
cat > /etc/docker/daemon.json << 'EOF'
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  },
  "no-new-privileges": true,
  "live-restore": true
}
EOF

systemctl reload docker
success "Docker daemon: log rotation, no-new-privileges, live-restore."

# ==============================================================================
# 12. SECRET FILES
#
#  Docker Compose mounts these files into containers at /run/secrets/<name>.
#  Spring Boot reads them via: spring.config.import: configtree:/run/secrets/
#  Postgres reads POSTGRES_PASSWORD_FILE=/run/secrets/postgres_password.
#  Redis & UI read them inline with $(cat /run/secrets/...).
#
#  The directory is owned by root:root, mode 700.
#  Each file is mode 400 (root read-only) — Docker bind-mounts them into the
#  container where the process reads them once at startup.
# ==============================================================================
DEPLOY_HOME="/home/$DEPLOY_USER"
APP_DIR="$DEPLOY_HOME/bugzkit"
SECRETS_DIR="$APP_DIR/secrets"

mkdir -p "$SECRETS_DIR"
chmod 700 "$APP_DIR" "$SECRETS_DIR"

write_secret() {
  local name="$1" value="$2"
  printf '%s' "$value" > "$SECRETS_DIR/$name"
  chmod 400 "$SECRETS_DIR/$name"
}

info "Writing secret files to $SECRETS_DIR …"
write_secret postgres_password    "$POSTGRES_PASSWORD"
write_secret redis_password       "$REDIS_PASSWORD"
write_secret jwt_secret           "$JWT_SECRET"
write_secret smtp_password        "$SMTP_PASSWORD"
write_secret user_password        "$USER_PASSWORD"
write_secret google_client_id     "$GOOGLE_CLIENT_ID"
write_secret google_client_secret "$GOOGLE_CLIENT_SECRET"

chown -R root:root "$SECRETS_DIR"
success "7 secret files written (mode 400, root-owned)."

# ==============================================================================
# 13. DEPLOY
# ==============================================================================
COMPOSE_FILE="$APP_DIR/docker-compose.prod.yml"

if [[ ! -f "$COMPOSE_FILE" ]]; then
  warn "docker-compose.prod.yml not found at $APP_DIR — skipping deploy."
  echo ""
  echo "  Copy the file manually, then run:"
  echo "    cd $APP_DIR"
  echo "    ACME_EMAIL=$ACME_EMAIL docker compose -f docker-compose.prod.yml up -d"
else
  info "Deploying stack…"
  cd "$APP_DIR"
  ACME_EMAIL="$ACME_EMAIL" docker compose -f docker-compose.prod.yml up -d
  success "Stack deployed."
  echo ""
  info "Service status:  docker compose -f $COMPOSE_FILE ps"
  info "Follow logs:     docker compose -f $COMPOSE_FILE logs -f bugzkit-api"
fi

# ==============================================================================
# DONE
# ==============================================================================
echo ""
echo -e "${GREEN}============================================================${NC}"
echo -e "${GREEN}  VPS initialization complete!${NC}"
echo -e "${GREEN}============================================================${NC}"
echo ""
echo -e "  Deploy user : ${YELLOW}${DEPLOY_USER}${NC}"
echo -e "  App dir     : ${YELLOW}${APP_DIR}${NC}"
echo -e "  Secrets dir : ${YELLOW}${SECRETS_DIR}${NC}  (root:root, mode 700/400)"
echo -e "  Firewall    : UFW — ports 22, 80, 443 open"
echo -e "  Fail2ban    : SSH (3 retries → 24h ban)"
echo ""
echo -e "${RED}IMPORTANT:${NC} Before closing this session, open a NEW terminal and"
echo -e "verify SSH access as '${DEPLOY_USER}' using your key:"
echo ""
echo -e "    ssh ${DEPLOY_USER}@<server-ip>"
echo ""
echo "Root login is now disabled. If locked out, use the DigitalOcean console."
echo ""
