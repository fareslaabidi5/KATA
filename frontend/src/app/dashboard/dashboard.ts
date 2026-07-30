import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  Message,
  Notification,
  QueuePreview,
  QueueStatus,
  Stats
} from '../services/message';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css'
})
export class Dashboard implements OnInit {

  private readonly messageService = inject(Message);

  messageText = '';
  messageId: number | null = null;

  messages: Notification[] = [];
  selectedMessage: Notification | null = null;

  queuePreview: QueuePreview[] = [];

  queueName = '';
  pendingMessages = 0;
  totalNotificationsSaved = 0;

  loading = false;
  sending = false;

  successMessage = '';
  errorMessage = '';

  ngOnInit(): void {
    this.refreshAll();
  }
  formatNotification(notification: Notification): string {
    return JSON.stringify(notification, null, 2);
  }
  refreshAll(): void {
    this.loading = true;
    this.clearMessages();

    this.loadMessages();
    this.loadQueueStatus();
    this.loadQueuePreview();
    this.loadStats();

    setTimeout(() => {
      this.loading = false;
    }, 500);
  }

  loadMessages(): void {
    this.messageService.getAllMessages().subscribe({
      next: (data: Notification[]) => {
        this.messages = data;
      },
      error: (error: unknown) => {
        console.error('Error loading messages', error);
        this.showError('Unable to load saved notifications.');
      }
    });
  }

  loadQueueStatus(): void {
    this.messageService.getQueueStatus().subscribe({
      next: (data: QueueStatus) => {
        this.queueName = data.queue;
        this.pendingMessages = data.pendingMessages;
      },
      error: (error: unknown) => {
        console.error('Error loading queue status', error);
        this.showError(
          'Unable to retrieve IBM MQ queue status.'
        );
      }
    });
  }

  loadQueuePreview(): void {
    this.messageService.previewQueueMessages().subscribe({
      next: (data: QueuePreview[]) => {
        this.queuePreview = data;
        this.pendingMessages = data.length;
      },
      error: (error: unknown) => {
        console.error('Error loading queue preview', error);
        this.showError(
          'Unable to preview queue messages.'
        );
      }
    });
  }

  loadStats(): void {
    this.messageService.getStats().subscribe({
      next: (data: Stats) => {
        this.totalNotificationsSaved =
          data.totalNotificationsSaved;
      },
      error: (error: unknown) => {
        console.error('Error loading statistics', error);
        this.showError(
          'Unable to load statistics.'
        );
      }
    });
  }

  sendMessage(): void {
    const text = this.messageText.trim();

    if (!text) {
      return;
    }

    this.sending = true;
    this.clearMessages();

    this.messageService.sendMessage(text).subscribe({
      next: (response: string) => {
        this.messageText = '';
        this.showSuccess(response);

        this.loadQueueStatus();
        this.loadQueuePreview();

        this.sending = false;
      },
      error: (error: unknown) => {
        console.error('Error sending message', error);
        this.showError(
          'Unable to send message to IBM MQ.'
        );
        this.sending = false;
      }
    });
  }

  findMessage(): void {
    if (!this.messageId) {
      return;
    }

    this.clearMessages();

    this.messageService
      .getMessageById(this.messageId)
      .subscribe({
        next: (data: Notification) => {
          this.selectedMessage = data;
        },
        error: (error: any) => {
          console.error('Error finding message', error);

          this.selectedMessage = null;

          if (error.status === 404) {
            this.showError(
              `Notification ${this.messageId} not found.`
            );
          } else {
            this.showError(
              'Unable to retrieve notification.'
            );
          }
        }
      });
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  private showSuccess(message: string): void {
    this.errorMessage = '';
    this.successMessage = message;

    setTimeout(() => {
      this.successMessage = '';
    }, 5000);
  }

  private showError(message: string): void {
    this.successMessage = '';
    this.errorMessage = message;
  }
}
