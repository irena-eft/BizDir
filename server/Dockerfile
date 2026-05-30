# Minimal PHP server for the BizDir Supabase proxy.
# Used to deploy the server/ folder to Render (or any Docker host).
FROM php:8.2-cli

WORKDIR /app
COPY . /app/

# Render passes the listening port through the PORT env var. We fall
# back to 10000 so the same image can be run locally with `docker run`.
ENV PORT=10000
EXPOSE 10000

# PHP's built-in development server is more than enough for the scale
# of this project. -t tells it which directory to serve.
CMD ["sh", "-c", "php -S 0.0.0.0:${PORT} -t /app"]
