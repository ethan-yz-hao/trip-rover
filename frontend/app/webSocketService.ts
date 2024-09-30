import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { PlanUpdateMessage } from '@/app/model';

class WebSocketService {
    private client: Client;
    private planId: number;
    private connected: boolean = false;

    constructor(planId: number) {
        const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';
        this.planId = planId;
        this.client = new Client({
            brokerURL: `${backendUrl.replace(/^http/, 'ws')}/ws`, // Use WebSocket URL
            webSocketFactory: () => new SockJS(`${backendUrl}/ws`),
            reconnectDelay: 5000,
            debug: (str) => {
                console.log(str);
            },
        });
    }

    connect(onMessageReceived: (message: PlanUpdateMessage) => void) {
        this.client.onConnect = () => {
            this.connected = true;
            // Subscribe to plan updates
            this.client.subscribe(`/topic/plan/${this.planId}`, (message: IMessage) => {
                const updateMessage: PlanUpdateMessage = JSON.parse(message.body);
                onMessageReceived(updateMessage);
            });
        };

        this.client.onStompError = (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        };

        this.client.activate();
    }

    disconnect() {
        if (this.connected) {
            this.client.deactivate();
            this.connected = false;
        }
    }

    sendUpdate(updateMessage: PlanUpdateMessage) {
        if (this.connected) {
            this.client.publish({
                destination: `/app/plan/${this.planId}/update`,
                body: JSON.stringify(updateMessage),
            });
        } else {
            console.error('WebSocket is not connected.');
        }
    }
}

export default WebSocketService;
