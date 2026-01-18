# Frontend Documentation

## Overview

The GoComet Ride Hailing frontend is a modern React application that provides a real-time, interactive user interface for riders and drivers. It features a dark theme, modular component architecture, and seamless WebSocket integration for live updates.

## Technology Stack

### Core Framework
- **React 18.2.0** - Modern React with hooks and functional components
- **React DOM 18.2.0** - React rendering library
- **React Router DOM 6.20.1** - Client-side routing (if needed)

### Communication
- **Axios 1.6.2** - HTTP client for REST API calls
- **SockJS Client 1.6.1** - WebSocket client library
- **STOMP.js 7.0.0** - STOMP protocol over WebSocket

### Build Tools
- **React Scripts 5.0.1** - Create React App build tooling
- **Webpack** - Module bundler (via react-scripts)
- **Babel** - JavaScript transpiler (via react-scripts)

### Styling
- **CSS3** - Custom styling with:
  - CSS Variables for theming
  - Dark theme implementation
  - Modular component styles
  - Responsive design

## Project Structure

```
frontend/
├── public/
│   ├── index.html          # Main HTML file
│   └── test-dashboard.html # Test page
├── src/
│   ├── components/
│   │   ├── common/         # Reusable components
│   │   │   ├── Button.js/css
│   │   │   ├── Card.js/css
│   │   │   ├── Input.js/css
│   │   │   └── StatusBadge.js/css
│   │   ├── Dashboard.js/css
│   │   ├── DriverDetails.js/css
│   │   ├── DriverPanel.js/css
│   │   ├── RiderDetails.js/css
│   │   └── RideRequest.js/css
│   ├── services/
│   │   ├── api.js          # REST API client
│   │   └── websocket.js   # WebSocket client
│   ├── theme/
│   │   └── colors.js      # Color palette
│   ├── App.js/css         # Main app component
│   ├── index.js           # Entry point
│   └── index.css          # Global styles
├── Dockerfile             # Docker configuration
├── nginx.conf             # Nginx configuration
└── package.json           # Dependencies and scripts
```

## Key Components

### 1. App.js (Main Application)

**Purpose**: Root component that manages navigation and routing

**Features**:
- Tab-based navigation (5 panels)
- State persistence using `localStorage`
- Active tab persists across page refreshes
- Component remounting with `key` props

**Navigation Tabs**:
1. Dashboard - Real-time ride overview
2. Request Ride - Create new ride requests
3. Driver Panel - Driver operations
4. Rider Details - View/edit rider information
5. Driver Details - View/edit driver information

### 2. Dashboard Component

**Purpose**: Real-time dashboard showing ride statistics and live updates

**Features**:
- Fetches all rides on mount
- WebSocket integration for live updates
- Real-time statistics:
  - Total rides
  - Active rides
  - Completed rides
  - Pending rides
- Auto-refresh functionality
- Manual refresh button

**Data Display**:
- Ride list with status badges
- Color-coded status indicators
- Real-time updates via WebSocket

### 3. RideRequest Component

**Purpose**: Form for riders to request a new ride

**Features**:
- Form validation
- Preset location options
- Vehicle tier selection
- Payment method selection
- Real-time form feedback
- Success/error notifications

**Form Fields**:
- Rider ID
- Pickup location (lat/lng)
- Destination (lat/lng)
- Vehicle tier (ECONOMY, PREMIUM, LUXURY)
- Payment method

### 4. DriverPanel Component

**Purpose**: Interface for drivers to manage rides

**Features**:
- Driver ID input
- Location update functionality
- View pending ride requests
- Accept ride requests
- Start/pause/resume/end trips
- Active ride state persistence

**Functionality**:
- Update driver location
- Fetch pending rides
- Accept ride assignments
- Manage trip lifecycle
- Real-time status updates

### 5. RiderDetails Component

**Purpose**: View and manage rider information

**Features**:
- View rider details by ID
- Rider ride history
- Create new rider
- Update existing rider
- Form validation
- Error handling

**Modes**:
- **View Mode**: Display rider information and ride history
- **Create Mode**: Form to create new rider
- **Edit Mode**: Form to update rider information

### 6. DriverDetails Component

**Purpose**: View and manage driver information

**Features**:
- View driver details by ID
- Driver ride history
- Create new driver
- Update existing driver
- Form validation
- Error handling

**Modes**:
- **View Mode**: Display driver information and ride history
- **Create Mode**: Form to create new driver
- **Edit Mode**: Form to update driver information

### 7. Common Components

#### Button Component
- Reusable button with variants
- Primary, secondary, danger styles
- Disabled state support
- Loading state support

#### Card Component
- Container for content sections
- Consistent styling
- Padding and spacing

#### Input Component
- Form input with validation
- Error message display
- Placeholder support
- Required field indicators

#### StatusBadge Component
- Color-coded status indicators
- Different status types:
  - PENDING (yellow)
  - MATCHED (blue)
  - IN_PROGRESS (green)
  - COMPLETED (gray)
  - CANCELLED (red)
  - FAILED (red)

## Services

### 1. api.js (REST API Client)

**Purpose**: Centralized HTTP client for backend API calls

**Configuration**:
- Base URL: `http://localhost:8080`
- Axios instance with interceptors
- Error handling
- Request/response logging

**API Methods**:
- `getAllRides()` - Fetch all rides
- `getRideById(id)` - Get ride details
- `createRide(rideData)` - Create new ride
- `cancelRide(id)` - Cancel a ride
- `getRiderById(id)` - Get rider details
- `createRider(riderData)` - Create new rider
- `updateRider(id, riderData)` - Update rider
- `getDriverById(id)` - Get driver details
- `createDriver(driverData)` - Create new driver
- `updateDriver(id, driverData)` - Update driver
- `updateDriverLocation(driverId, location)` - Update location
- `acceptRide(driverId, acceptData)` - Accept ride
- `getPendingRides(driverId)` - Get pending rides
- `startTrip(tripId)` - Start trip
- `pauseTrip(tripId)` - Pause trip
- `resumeTrip(tripId)` - Resume trip
- `endTrip(tripId, endData)` - End trip
- `processPayment(paymentData)` - Process payment

### 2. websocket.js (WebSocket Client)

**Purpose**: WebSocket connection for real-time updates

**Configuration**:
- SockJS client
- STOMP protocol
- Auto-reconnect on disconnect
- Connection state management

**Subscriptions**:
- `/topic/rides` - Ride updates
- `/topic/drivers/{driverId}` - Driver-specific updates
- `/topic/riders/{riderId}` - Rider-specific updates

**Features**:
- Connection management
- Message handling
- Error handling
- Reconnection logic

## Styling & Theming

### Dark Theme Implementation

**CSS Variables** (defined in `index.css`):
```css
:root {
  --bg-primary: #1a1a1a;
  --bg-secondary: #2d2d2d;
  --bg-tertiary: #3a3a3a;
  --text-primary: #ffffff;
  --text-secondary: #b0b0b0;
  --accent-primary: #4a9eff;
  --accent-secondary: #6bb6ff;
  --success: #4caf50;
  --warning: #ff9800;
  --error: #f44336;
  --border: #404040;
}
```

### Component Styles

Each component has its own CSS file:
- Modular styling approach
- Component-scoped styles
- Consistent design language
- Responsive design

### Color Palette

Defined in `theme/colors.js`:
- Primary colors
- Accent colors
- Status colors
- Background colors
- Text colors

## State Management

### Local State
- Component-level state using `useState`
- Form state management
- UI state (loading, errors, etc.)

### Persistent State
- `localStorage` for:
  - Active tab persistence
  - Active ride state (DriverPanel)
  - User preferences

### Real-time State
- WebSocket subscriptions for live updates
- Automatic UI updates on data changes

## User Experience Features

### 1. Real-time Updates
- Live ride status changes
- Instant notifications
- No page refresh needed

### 2. Form Validation
- Client-side validation
- Real-time error feedback
- Required field indicators

### 3. Error Handling
- User-friendly error messages
- Network error handling
- Retry mechanisms

### 4. Loading States
- Loading indicators
- Disabled buttons during operations
- Progress feedback

### 5. Responsive Design
- Mobile-friendly layout
- Adaptive component sizing
- Touch-friendly interactions

## Build & Development

### Development Setup

1. **Install Dependencies**:
   ```bash
   cd frontend
   npm install
   ```

2. **Start Development Server**:
   ```bash
   npm start
   ```
   - Runs on `http://localhost:3000`
   - Hot reload enabled
   - Proxy to backend at `http://localhost:8080`

### Production Build

1. **Build for Production**:
   ```bash
   npm run build
   ```
   - Creates optimized production build
   - Minified JavaScript and CSS
   - Asset optimization

2. **Serve Production Build**:
   ```bash
   npm install -g serve
   serve -s build
   ```

### Docker Build

The frontend is containerized using:
- **Base Image**: `node:18-alpine`
- **Build Stage**: Install dependencies and build
- **Runtime**: Nginx to serve static files

**Dockerfile**:
```dockerfile
# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Runtime stage
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
```

## Configuration

### Environment Variables

- `REACT_APP_API_URL` - Backend API URL
- `REACT_APP_WS_URL` - WebSocket URL

### Proxy Configuration

In `package.json`:
```json
{
  "proxy": "http://localhost:8080"
}
```

This proxies API requests to the backend during development.

## Testing

### Manual Testing
- Test all user flows
- Verify real-time updates
- Test form validations
- Check error handling

### Browser Compatibility
- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Performance Optimizations

1. **Code Splitting**
   - Lazy loading of components
   - Route-based code splitting

2. **Memoization**
   - React.memo for expensive components
   - useMemo for computed values
   - useCallback for event handlers

3. **Asset Optimization**
   - Minified JavaScript and CSS
   - Optimized images
   - Gzip compression

4. **Caching**
   - Browser caching for static assets
   - Service worker for offline support (if implemented)

## Best Practices Implemented

1. **Component Architecture**
   - Reusable components
   - Single responsibility principle
   - Props validation

2. **Code Organization**
   - Modular file structure
   - Separation of concerns
   - Service layer for API calls

3. **Error Handling**
   - Try-catch blocks
   - Error boundaries (if implemented)
   - User-friendly error messages

4. **Accessibility**
   - Semantic HTML
   - ARIA labels (where needed)
   - Keyboard navigation support

5. **Performance**
   - Optimized re-renders
   - Efficient state management
   - Lazy loading

## Deployment

### Docker Deployment
```bash
docker-compose up frontend
```

### Static Hosting
- Build the application
- Deploy `build/` folder to:
  - AWS S3 + CloudFront
  - Netlify
  - Vercel
  - GitHub Pages

## Troubleshooting

### Common Issues

1. **WebSocket Connection Failed**
   - Check backend is running
   - Verify WebSocket URL
   - Check CORS configuration

2. **API Calls Failing**
   - Verify backend URL
   - Check network connectivity
   - Review browser console for errors

3. **State Not Persisting**
   - Check localStorage is enabled
   - Verify key names are consistent
   - Check browser storage limits

## Future Enhancements

1. **State Management**
   - Redux or Zustand for global state
   - Better state persistence

2. **Testing**
   - Unit tests with Jest
   - Component tests with React Testing Library
   - E2E tests with Cypress

3. **Performance**
   - Service workers for offline support
   - Progressive Web App (PWA) features
   - Image optimization

4. **Features**
   - Real-time map integration
   - Push notifications
   - Advanced filtering and search
