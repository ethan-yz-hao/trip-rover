import { Client, IMessage, IFrame } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { PlanAckMessage, PlanUpdateMessage } from "@/types/model";
import { v4 as uuidv4 } from "uuid";
import log from "@/lib/log";
import { AppError } from "./axios";

class WebSocketService {
    private client: Client;
    private planId: number;
    private connected: boolean = false;
    private clientId: string;
    private connectionPromise: Promise<void> | null = null;

    constructor(planId: number) {
        const backendUrl =
            process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";
        this.planId = planId;
        this.clientId = uuidv4(); // Unique client ID
        this.client = new Client({
            brokerURL: `${backendUrl.replace(/^http/, "ws")}/ws`, // Use WebSocket URL
            webSocketFactory: () => new SockJS(`${backendUrl}/ws`),
            reconnectDelay: 5000,
            debug: (str) => {
                log.info(str);
            },
        });
    }

    async connect(
        onMessageReceived: (message: PlanUpdateMessage) => void,
        onAckReceived: (message: PlanAckMessage) => void
    ): Promise<void> {
        if (this.connectionPromise) {
            return this.connectionPromise;
        }

        this.connectionPromise = new Promise((resolve, reject) => {
            const timeoutId = setTimeout(() => {
                reject(new AppError("WebSocket connection timeout", 408));
            }, 10000); // 10 second timeout

            this.client.onConnect = () => {
                clearTimeout(timeoutId);
                this.connected = true;

                try {
                    // Subscribe to plan updates
                    this.client.subscribe(
                        `/topic/plan/${this.planId}`,
                        (message: IMessage) => {
                            try {
                                const updateMessage: PlanUpdateMessage =
                                    JSON.parse(message.body);
                                onMessageReceived(updateMessage);
                            } catch (err) {
                                log.error("Error parsing update message:", err);
                                throw new AppError(
                                    "Invalid update message format",
                                    422,
                                    err
                                );
                            }
                        }
                    );

                    // Subscribe to client-specific ack messages
                    this.client.subscribe(
                        `/topic/plan/${this.planId}/ack/${this.clientId}`,
                        (message: IMessage) => {
                            try {
                                const ackMessage: PlanAckMessage = JSON.parse(
                                    message.body
                                );
                                onAckReceived(ackMessage);
                            } catch (err) {
                                log.error("Error parsing ack message:", err);
                                throw new AppError(
                                    "Invalid ack message format",
                                    422,
                                    err
                                );
                            }
                        }
                    );

                    resolve();
                } catch (err) {
                    reject(
                        new AppError("Failed to subscribe to topics", 500, err)
                    );
                }
            };

            this.client.onStompError = (frame: IFrame) => {
                clearTimeout(timeoutId);
                const error = new AppError(
                    frame.headers["message"] || "WebSocket error",
                    parseInt(frame.headers["code"] || "500"),
                    frame.body
                );
                reject(error);
            };

            this.client.onWebSocketError = (event: Event) => {
                clearTimeout(timeoutId);
                reject(new AppError("WebSocket connection error", 503, event));
            };

            try {
                this.client.activate();
            } catch (err) {
                clearTimeout(timeoutId);
                reject(
                    new AppError(
                        "Failed to activate WebSocket connection",
                        500,
                        err
                    )
                );
            }
        });

        return this.connectionPromise;
    }

    disconnect(): void {
        if (this.connected) {
            this.client.deactivate();
            this.connected = false;
            this.connectionPromise = null;
        }
    }

    sendUpdate(
        updateMessage: PlanUpdateMessage,
        updateId: string
    ): string | null {
        if (!this.connected) {
            throw new AppError("WebSocket is not connected", 409);
        }

        try {
            const messageWithId = {
                ...updateMessage,
                clientId: this.clientId,
                updateId,
            };

            this.client.publish({
                destination: `/app/plan/${this.planId}/update`,
                body: JSON.stringify(messageWithId),
            });

            return updateId;
        } catch (err) {
            throw new AppError("Failed to send update", 500, err);
        }
    }

    getClientId(): string {
        return this.clientId;
    }

    isConnected(): boolean {
        return this.connected;
    }
}

export default WebSocketService;
