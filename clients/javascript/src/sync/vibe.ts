import { SyncBridge } from './bridge';
import { ElementSync } from './element';

export class VibeSync {
  private bridge: SyncBridge;

  constructor(bridge: SyncBridge) {
    this.bridge = bridge;
  }

  go(url: string): void {
    this.bridge.call('go', [url]);
  }

  screenshot(): Buffer {
    const result = this.bridge.call<{ data: string }>('screenshot');
    return Buffer.from(result.data, 'base64');
  }

  find(selector: string): ElementSync {
    const result = this.bridge.call<{ elementId: number }>('find', [selector]);
    return new ElementSync(this.bridge, result.elementId);
  }

  quit(): void {
    this.bridge.call('quit');
    this.bridge.terminate();
  }
}
