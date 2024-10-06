import {Client, IMessage} from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import {PlanAckMessage, PlanUpdateMessage} from '@/app/model';
import {v4 as uuidv4} from 'uuid';
import log from "@/app/log";

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
                log.info(str);
            },
        });
    }

    connect(
        onMessageReceived: (message: PlanUpdateMessage) => void,
        onAckReceived: (message: PlanAckMessage) => void
    ) {
        this.client.onConnect = () => {
            this.connected = true;
            // Subscribe to plan updates
            this.client.subscribe(`/topic/plan/${this.planId}`, (message: IMessage) => {
                const updateMessage: PlanUpdateMessage = JSON.parse(message.body);
                onMessageReceived(updateMessage);
            });
            // Subscribe to client-specific ack messages
            this.client.subscribe(`/topic/plan/${this.planId}/ack/${this.clientId}`, (message: IMessage) => {
                const ackMessage: PlanAckMessage = JSON.parse(message.body);
                onAckReceived(ackMessage);
            });
        };

        this.client.onStompError = (frame) => {
            log.error('Broker reported error: ' + frame.headers['message']);
            log.error('Additional details: ' + frame.body);
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
            log.error('WebSocket is not connected.');
            return null;
        }
    }

    getClientId() {
        return this.clientId;
    }
}

export default WebSocketService;
