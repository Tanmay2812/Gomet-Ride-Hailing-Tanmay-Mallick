#!/bin/bash

# New Relic Setup Script for GoComet Ride Hailing

set -e

echo "🚀 New Relic Setup for GoComet Ride Hailing"
echo "============================================="
echo ""

# Check if .env file exists
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    touch .env
    echo "# New Relic Configuration" >> .env
    echo "# Get your license key from: https://one.newrelic.com/admin-portal/api-keys/home" >> .env
    echo "" >> .env
fi

# Check if license key is already set
if grep -q "NEW_RELIC_LICENSE_KEY=" .env 2>/dev/null; then
    CURRENT_KEY=$(grep "NEW_RELIC_LICENSE_KEY=" .env | cut -d'=' -f2)
    if [ "$CURRENT_KEY" != "" ] && [ "$CURRENT_KEY" != "your-license-key" ]; then
        echo "✅ New Relic license key already configured in .env"
        echo "   Current key: ${CURRENT_KEY:0:10}..."
        read -p "Do you want to update it? (y/n): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo "Keeping existing configuration."
            exit 0
        fi
    fi
fi

echo ""
echo "📋 Step 1: Create New Relic Account"
echo "-----------------------------------"
echo "1. Go to: https://newrelic.com"
echo "2. Click 'Sign up' or 'Start free'"
echo "3. Complete registration (100GB free tier available)"
echo ""
read -p "Press Enter when you've created your account..."

echo ""
echo "📋 Step 2: Get Your License Key"
echo "-------------------------------"
echo "1. Go to: https://one.newrelic.com/admin-portal/api-keys/home"
echo "2. Find your 'License key' (starts with NRAL-...)"
echo "3. Copy the license key"
echo ""
read -p "Enter your New Relic License Key: " LICENSE_KEY

if [ -z "$LICENSE_KEY" ]; then
    echo "❌ License key cannot be empty!"
    exit 1
fi

# Update .env file
if grep -q "NEW_RELIC_LICENSE_KEY=" .env; then
    # Update existing key
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/NEW_RELIC_LICENSE_KEY=.*/NEW_RELIC_LICENSE_KEY=$LICENSE_KEY/" .env
    else
        # Linux
        sed -i "s/NEW_RELIC_LICENSE_KEY=.*/NEW_RELIC_LICENSE_KEY=$LICENSE_KEY/" .env
    fi
else
    # Add new key
    echo "NEW_RELIC_LICENSE_KEY=$LICENSE_KEY" >> .env
fi

echo ""
read -p "Enter your New Relic Account ID (optional, press Enter to skip): " ACCOUNT_ID

if [ ! -z "$ACCOUNT_ID" ]; then
    if grep -q "NEW_RELIC_ACCOUNT_ID=" .env; then
        if [[ "$OSTYPE" == "darwin"* ]]; then
            sed -i '' "s/NEW_RELIC_ACCOUNT_ID=.*/NEW_RELIC_ACCOUNT_ID=$ACCOUNT_ID/" .env
        else
            sed -i "s/NEW_RELIC_ACCOUNT_ID=.*/NEW_RELIC_ACCOUNT_ID=$ACCOUNT_ID/" .env
        fi
    else
        echo "NEW_RELIC_ACCOUNT_ID=$ACCOUNT_ID" >> .env
    fi
fi

echo ""
echo "✅ Configuration saved to .env file"
echo ""

# Check if docker-compose.yml needs updating
if ! grep -q "env_file:" docker-compose.yml 2>/dev/null; then
    echo "📝 Updating docker-compose.yml to use .env file..."
    # This would require manual editing or a more complex script
    echo "⚠️  Please manually update docker-compose.yml backend service to include:"
    echo "   env_file:"
    echo "     - .env"
    echo ""
fi

echo "📋 Step 3: Restart Services"
echo "----------------------------"
read -p "Do you want to restart the backend service now? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🔄 Restarting backend service..."
    docker-compose up -d --build backend
    echo ""
    echo "⏳ Waiting for backend to start..."
    sleep 10
    
    echo ""
    echo "📊 Checking New Relic connection..."
    if docker-compose logs backend | grep -i "new relic\|newrelic" | tail -5; then
        echo ""
        echo "✅ Backend restarted successfully!"
    else
        echo ""
        echo "⚠️  Check logs: docker-compose logs backend"
    fi
fi

echo ""
echo "✅ Setup Complete!"
echo "=================="
echo ""
echo "📋 Next Steps:"
echo "1. Wait 2-3 minutes for data to appear in New Relic"
echo "2. Go to: https://one.newrelic.com"
echo "3. Navigate to 'APM & Services'"
echo "4. Look for 'GoComet-Ride-Hailing' application"
echo ""
echo "📚 For more details, see: docs/NEW_RELIC.md"
echo ""
echo "🧪 Test the setup:"
echo "   curl http://localhost:8080/v1/rides?limit=10"
echo "   # Then check New Relic dashboard in 1-2 minutes"
echo ""
