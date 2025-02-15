## Running the Frontend

Ensure the API is up and running before starting the UI. Then, create a `.env` file in the root of the frontend directory with the following content:

```bash
# Public
PUBLIC_APP_NAME=your_app_name
PUBLIC_API_URL=http://localhost:8080/v1
# Private
JWT_SECRET=secret
```

To start the UI, run:

```bash
pnpm install
pnpm run dev
```

You're all set!
