// Polyfill completo para ResizeObserver que evita el error de bucle infinito

const debounce = (func: Function, wait: number) => {
  let timeout: NodeJS.Timeout;
  return function executedFunction(...args: any[]) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
};

// Interceptar y suprimir todos los errores relacionados con ResizeObserver
const suppressResizeObserverErrors = () => {
  // Capturar errores de ResizeObserver en diferentes contextos
  const originalError = window.console.error;
  window.console.error = (...args) => {
    const errorMessage = args.join(' ');
    if (
      errorMessage.includes('ResizeObserver loop') ||
      errorMessage.includes('ResizeObserver loop completed with undelivered notifications') ||
      errorMessage.includes('ResizeObserver loop limit exceeded')
    ) {
      return; // Suprimir el error
    }
    originalError.apply(console, args);
  };

  // Interceptar errores no manejados
  window.addEventListener('error', (event) => {
    if (
      event.message &&
      (event.message.includes('ResizeObserver loop') ||
       event.message.includes('ResizeObserver loop completed with undelivered notifications') ||
       event.message.includes('ResizeObserver loop limit exceeded'))
    ) {
      event.preventDefault();
      event.stopPropagation();
      return false;
    }
  });

  // Interceptar errores de promesas rechazadas
  window.addEventListener('unhandledrejection', (event) => {
    if (
      event.reason &&
      typeof event.reason === 'string' &&
      (event.reason.includes('ResizeObserver loop') ||
       event.reason.includes('ResizeObserver loop completed with undelivered notifications'))
    ) {
      event.preventDefault();
    }
  });
};

if (typeof window !== 'undefined') {
  // Aplicar supresión de errores
  suppressResizeObserverErrors();

  // Mejorar ResizeObserver si existe
  if (window.ResizeObserver) {
    const OriginalResizeObserver = window.ResizeObserver;
    
    class ImprovedResizeObserver extends OriginalResizeObserver {
      constructor(callback: ResizeObserverCallback) {
        // Debounce y manejo seguro de callbacks
        const safeCallback = debounce((entries: ResizeObserverEntry[], observer: ResizeObserver) => {
          try {
            // Usar requestAnimationFrame para sincronizar con el ciclo de renderizado
            requestAnimationFrame(() => {
              try {
                callback(entries, observer);
              } catch (error) {
                // Suprimir errores específicos de ResizeObserver
                if (
                  error instanceof Error &&
                  (error.message.includes('ResizeObserver loop') ||
                   error.message.includes('ResizeObserver loop completed with undelivered notifications'))
                ) {
                  return;
                }
                // Re-lanzar otros errores
                throw error;
              }
            });
          } catch (error) {
            // Manejar errores en requestAnimationFrame
            if (
              error instanceof Error &&
              (error.message.includes('ResizeObserver loop') ||
               error.message.includes('ResizeObserver loop completed with undelivered notifications'))
            ) {
              return;
            }
            console.error('Error in ResizeObserver callback:', error);
          }
        }, 16); // ~60fps
        
        super(safeCallback);
      }
    }
    
    window.ResizeObserver = ImprovedResizeObserver;
  }
}

export {};
