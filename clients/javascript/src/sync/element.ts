import { SyncBridge } from './bridge';
import { BoundingBox } from '../element';

export class ElementSync {
  private bridge: SyncBridge;
  private elementId: number;

  constructor(bridge: SyncBridge, elementId: number) {
    this.bridge = bridge;
    this.elementId = elementId;
  }

  click(): void {
    this.bridge.call('element.click', [this.elementId]);
  }

  type(text: string): void {
    this.bridge.call('element.type', [this.elementId, text]);
  }

  text(): string {
    const result = this.bridge.call<{ text: string }>('element.text', [this.elementId]);
    return result.text;
  }

  getAttribute(name: string): string | null {
    const result = this.bridge.call<{ value: string | null }>('element.getAttribute', [this.elementId, name]);
    return result.value;
  }

  boundingBox(): BoundingBox {
    const result = this.bridge.call<{ box: BoundingBox }>('element.boundingBox', [this.elementId]);
    return result.box;
  }
}
