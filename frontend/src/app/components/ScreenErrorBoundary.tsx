import { Component, type ErrorInfo, type ReactNode } from 'react';

type ScreenErrorBoundaryProps = {
  children: ReactNode;
  resetKey: string;
};

type ScreenErrorBoundaryState = {
  error: Error | null;
};

export class ScreenErrorBoundary extends Component<ScreenErrorBoundaryProps, ScreenErrorBoundaryState> {
  state: ScreenErrorBoundaryState = { error: null };

  static getDerivedStateFromError(error: Error): ScreenErrorBoundaryState {
    return { error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Error renderizando pantalla Servify', error, errorInfo);
  }

  componentDidUpdate(prevProps: ScreenErrorBoundaryProps) {
    if (prevProps.resetKey !== this.props.resetKey && this.state.error) {
      this.setState({ error: null });
    }
  }

  render() {
    if (!this.state.error) return this.props.children;

    return (
      <div className='flex h-full flex-col items-center justify-center gap-4 bg-white px-6 text-center'>
        <div className='rounded-2xl px-4 py-3' style={{ background: '#fef2f2', border: '1.5px solid #fecaca' }}>
          <p style={{ color: '#991b1b', fontSize: 15, fontWeight: 900 }}>No se pudo abrir esta pantalla</p>
          <p style={{ color: '#b91c1c', fontSize: 12, fontWeight: 700, lineHeight: 1.45, marginTop: 6 }}>
            La app recupero la navegacion para evitar que la pantalla quede en blanco.
          </p>
          <p style={{ color: '#7f1d1d', fontSize: 11, fontWeight: 700, lineHeight: 1.35, marginTop: 8 }}>
            {this.state.error.message}
          </p>
        </div>
        <button
          type='button'
          onClick={() => window.location.reload()}
          className='rounded-2xl px-5 py-3 transition-all active:scale-95'
          style={{ background: '#2563eb', color: 'white', fontSize: 14, fontWeight: 900 }}
        >
          Recargar app
        </button>
      </div>
    );
  }
}