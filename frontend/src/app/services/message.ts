import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Notification {
  id?: number;
  message?: string;
  correlationId?: string;
  createdAt?: string;
  [key: string]: unknown;
}

export interface QueueStatus {
  queue: string;
  pendingMessages: number;
}

export interface QueuePreview {
  correlationId: string;
  body: string;
}

export interface Stats {
  totalNotificationsSaved: number;
}

@Injectable({
  providedIn: 'root',
})
export class Message {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8080/messages';

  getAllMessages(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.apiUrl);
  }

  getMessageById(id: number): Observable<Notification> {
    return this.http.get<Notification>(
      `${this.apiUrl}/${id}`
    );
  }

  sendMessage(message: string): Observable<string> {
    return this.http.post(
      this.apiUrl,
      message,
      {
        headers: {
          'Content-Type': 'text/plain'
        },
        responseType: 'text'
      }
    );
  }

  getQueueStatus(): Observable<QueueStatus> {
    return this.http.get<QueueStatus>(
      `${this.apiUrl}/queue-status`
    );
  }

  previewQueueMessages(): Observable<QueuePreview[]> {
    return this.http.get<QueuePreview[]>(
      `${this.apiUrl}/queue-status/preview`
    );
  }

  getStats(): Observable<Stats> {
    return this.http.get<Stats>(
      `${this.apiUrl}/stats`
    );
  }
}
