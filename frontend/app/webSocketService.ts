import {Client, IMessage} from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {PlanUpdateMessage} from '@/app/model';
import {v4 as uuidv4} from 'uuid';

class WebSocketService {
    private client: Client;
    private planId: number;
    private connected: boolean = false;
    private clientId: string;

    constructor(planId: number) {
        const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';
        this.planId = planId;
        this.clientId = uuidv4(); // Unique client ID
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

    sendUpdate(updateMessage: PlanUpdateMessage, updateId: string) {
        if (this.connected) {
            const messageWithId = {
                ...updateMessage,
                clientId: this.clientId,
                updateId,
            };

            this.client.publish({
                destination: `/app/plan/${this.planId}/update`,
                body: JSON.stringify(messageWithId),
            });
            return updateId; // Return updateId for tracking
        } else {
            console.error('WebSocket is not connected.');
            return null;
        }
    }

    getClientId() {
        return this.clientId;
    }
}

export default WebSocketService;
