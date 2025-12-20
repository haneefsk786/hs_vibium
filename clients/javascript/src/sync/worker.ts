import { parentPort, workerData } from 'worker_threads';
import { browser } from '../browser';
import { Vibe } from '../vibe';
import { Element } from '../element';

interface WorkerData {
  signal: Int32Array;
}

interface Command {
  id: number;
  method: string;
  args: unknown[];
}

const { signal } = workerData as WorkerData;

let vibe: Vibe | null = null;
let elements: Map<number, Element> = new Map();
let nextElementId = 1;

async function handleCommand(cmd: Command): Promise<unknown> {
  switch (cmd.method) {
    case 'launch': {
      const [options] = cmd.args as [{ headless?: boolean } | undefined];
      vibe = await browser.launch(options);
      return { success: true };
    }

    case 'go': {
      if (!vibe) throw new Error('Browser not launched');
      const [url] = cmd.args as [string];
      await vibe.go(url);
      return { success: true };
    }

    case 'screenshot': {
      if (!vibe) throw new Error('Browser not launched');
      const buffer = await vibe.screenshot();
      return { data: buffer.toString('base64') };
    }

    case 'find': {
      if (!vibe) throw new Error('Browser not launched');
      const [selector] = cmd.args as [string];
      const element = await vibe.find(selector);
      const elementId = nextElementId++;
      elements.set(elementId, element);
      return { elementId };
    }

    case 'element.click': {
      const [elementId] = cmd.args as [number];
      const element = elements.get(elementId);
      if (!element) throw new Error(`Element ${elementId} not found`);
      await element.click();
      return { success: true };
    }

    case 'element.type': {
      const [elementId, text] = cmd.args as [number, string];
      const element = elements.get(elementId);
      if (!element) throw new Error(`Element ${elementId} not found`);
      await element.type(text);
      return { success: true };
    }

    case 'element.text': {
      const [elementId] = cmd.args as [number];
      const element = elements.get(elementId);
      if (!element) throw new Error(`Element ${elementId} not found`);
      const text = await element.text();
      return { text };
    }

    case 'element.getAttribute': {
      const [elementId, name] = cmd.args as [number, string];
      const element = elements.get(elementId);
      if (!element) throw new Error(`Element ${elementId} not found`);
      const value = await element.getAttribute(name);
      return { value };
    }

    case 'element.boundingBox': {
      const [elementId] = cmd.args as [number];
      const element = elements.get(elementId);
      if (!element) throw new Error(`Element ${elementId} not found`);
      const box = await element.boundingBox();
      return { box };
    }

    case 'quit': {
      if (!vibe) throw new Error('Browser not launched');
      await vibe.quit();
      vibe = null;
      elements.clear();
      return { success: true };
    }

    default:
      throw new Error(`Unknown method: ${cmd.method}`);
  }
}

parentPort!.on('message', async ({ cmd, port }: { cmd: Command; port: MessagePort }) => {
  let result: unknown;
  let error: string | null = null;

  try {
    result = await handleCommand(cmd);
  } catch (err) {
    error = err instanceof Error ? err.message : String(err);
  }

  // Send result through the port
  port.postMessage({ result, error });

  // Signal completion
  Atomics.store(signal, 0, 1);
  Atomics.notify(signal, 0);
});
