import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

class WebSocketService {
  constructor() {
    this.client = null;
    this.connected = false;
    this.subscriptions = new Map();
  }

  connect(onConnect, onError) {
    if (this.connected) {
      console.log('WebSocket already connected');
      return;
    }

    const socketUrl = process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws';

    this.client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        console.log('STOMP:', str);
      },
      onConnect: () => {
        console.log('WebSocket connected');
        this.connected = true;
        if (onConnect) onConnect();
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        this.connected = false;
        if (onError) onError(frame);
      },
      onWebSocketClose: () => {
        console.log('WebSocket closed');
        this.connected = false;
      },
    });

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.subscriptions.clear();
      this.client.deactivate();
      this.connected = false;
      console.log('WebSocket disconnected');
    }
  }

  subscribe(destination, callback) {
    if (!this.client || !this.connected) {
      console.error('WebSocket not connected. Cannot subscribe to', destination);
      return null;
    }

    const subscription = this.client.subscribe(destination, (message) => {
      try {
        const data = JSON.parse(message.body);
        callback(data);
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
      }
    });

    this.subscriptions.set(destination, subscription);
    console.log('Subscribed to', destination);
    return subscription;
  }

  unsubscribe(destination) {
    const subscription = this.subscriptions.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(destination);
      console.log('Unsubscribed from', destination);
    }
  }

  isConnected() {
    return this.connected;
  }
}

const wsService = new WebSocketService();
export default wsService;
