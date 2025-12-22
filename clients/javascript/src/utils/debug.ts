/**
 * Debug logging for the Vibium client library.
 * Enable by setting VIBIUM_DEBUG=1 environment variable.
 */

const isDebugEnabled = process.env.VIBIUM_DEBUG === '1' || process.env.VIBIUM_DEBUG === 'true';

/**
 * Log a debug message if VIBIUM_DEBUG is enabled.
 */
export function debug(message: string, data?: Record<string, unknown>): void {
  if (!isDebugEnabled) return;

  const timestamp = new Date().toISOString();
  const logObj = {
    time: timestamp,
    level: 'debug',
    msg: message,
    ...data,
  };

  console.error(JSON.stringify(logObj));
}

/**
 * Log an info message if VIBIUM_DEBUG is enabled.
 */
export function info(message: string, data?: Record<string, unknown>): void {
  if (!isDebugEnabled) return;

  const timestamp = new Date().toISOString();
  const logObj = {
    time: timestamp,
    level: 'info',
    msg: message,
    ...data,
  };

  console.error(JSON.stringify(logObj));
}

/**
 * Log a warning message if VIBIUM_DEBUG is enabled.
 */
export function warn(message: string, data?: Record<string, unknown>): void {
  if (!isDebugEnabled) return;

  const timestamp = new Date().toISOString();
  const logObj = {
    time: timestamp,
    level: 'warn',
    msg: message,
    ...data,
  };

  console.error(JSON.stringify(logObj));
}
