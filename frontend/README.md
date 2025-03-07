# Google Maps Integration Plan

This document outlines the plan for migrating the Google Maps functionality from the old frontend to the new frontend architecture, supporting both authenticated (with WebSocket) and unauthenticated (without WebSocket) scenarios.

## Architecture Overview

### State Management

-   Create new Redux slices in the new frontend for:
    -   `startTimeSlice`: To manage departure time
    -   `placesArraySlice`: To manage the collection of places
    -   `arrivalTimeArraySlice`: To manage arrival times

### Component Structure

1. **Map Components**:

    - `MapContainer`: Main wrapper component
    - `MapCanvas`: The actual Google Maps canvas
    - `CustomMarker`: Reusable marker component
    - `PlaceInfoCard`: Information card for places
    - `DateTimePicker`: For selecting start time

2. **Integration with WebSocket**:
    - Create a `MapSynchronizer` component that conditionally connects to WebSocket when user is authenticated
    - Implement optimistic updates for better UX

## Implementation Steps

### 1. Redux Store Setup

-   Add new slices to the existing store
-   Ensure compatibility with the existing auth slice

### 2. Core Map Components

-   Implement the map components using the @vis.gl/react-google-maps library
-   Create hooks for Google Maps services (Places, Directions, etc.)

### 3. WebSocket Integration

-   Create a higher-order component or context provider that:
    -   Checks authentication status
    -   Conditionally establishes WebSocket connection
    -   Synchronizes map state with server

### 4. Fallback Mode

-   Implement local-only functionality for unauthenticated users
-   Store temporary data in localStorage

### 5. UI Components

-   Implement the sidebar for place management
-   Create modals for sharing and collaboration (authenticated only)

## Component Breakdown

### MapContainer

-   Responsible for:
    -   Loading Google Maps API
    -   Managing viewport state
    -   Handling authentication status checks
    -   Conditionally enabling WebSocket features

### MapCanvas

-   Renders the actual map
-   Handles map interactions
-   Manages markers and routes

### PlaceManager

-   Manages the list of places
-   Handles drag-and-drop reordering
-   Syncs with WebSocket when authenticated

## Data Flow

### Authenticated Flow

1. User makes changes to the map/places
2. Changes are dispatched to Redux
3. WebSocket service sends updates to server
4. Server broadcasts changes to other clients
5. Other clients update their Redux store

### Unauthenticated Flow

1. User makes changes to the map/places
2. Changes are dispatched to Redux
3. Changes are stored in localStorage
4. User is prompted to create an account to save permanently

## Technical Considerations

### Google Maps API

-   Use the @vis.gl/react-google-maps library for React integration
-   Implement custom hooks for Places, Directions, and other Google Maps services

### Performance Optimization

-   Implement debouncing for search and map interactions
-   Use React.memo and useMemo for expensive components
-   Optimize marker rendering for large datasets

### Error Handling

-   Implement fallbacks for Google Maps API failures
-   Handle WebSocket disconnections gracefully
-   Provide meaningful error messages to users

## Migration Strategy

1. Start by implementing the core map functionality without WebSocket integration
2. Add authentication checks and conditional WebSocket connectivity
3. Implement the sharing and collaboration features for authenticated users
4. Add the localStorage fallback for unauthenticated users
5. Polish the UI and UX for both scenarios
