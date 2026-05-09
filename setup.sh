APP_NAME="ellipse"
APP_USER="jim"

AVATARS_DIR="/var/lib/$APP_NAME/avatars"

mkdir -p "$AVATARS_DIR"
chown "$APP_USER:$APP_USER" "$AVATARS_DIR"
chmod 750 "$AVATARS_DIR"

echo "avatars folder at $AVATARS_DIR"