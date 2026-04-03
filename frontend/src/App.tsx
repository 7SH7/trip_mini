import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { Provider } from 'react-redux'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material'
import { store } from './store'
import Layout from './components/layout/Layout'
import ProtectedRoute from './components/common/ProtectedRoute'
import AuthInitializer from './components/common/AuthInitializer'
import LoginPage from './pages/auth/LoginPage'
import RegisterPage from './pages/auth/RegisterPage'
import OAuthCallbackPage from './pages/auth/OAuthCallbackPage'
import HomePage from './pages/HomePage'
import TripsPage from './pages/trips/TripsPage'
import TripDetailPage from './pages/trips/TripDetailPage'
import CreateTripPage from './pages/trips/CreateTripPage'
import BookingsPage from './pages/bookings/BookingsPage'
import PaymentsPage from './pages/payments/PaymentsPage'
import AccommodationsPage from './pages/accommodation/AccommodationsPage'
import SubscriptionPage from './pages/subscription/SubscriptionPage'
import FeedPage from './pages/feed/FeedPage'
import ChatPage from './pages/chat/ChatPage'
import ChatRoomPage from './pages/chat/ChatRoomPage'
import NotificationsPage from './pages/notifications/NotificationsPage'
import LiveStreamsPage from './pages/streaming/LiveStreamsPage'

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, staleTime: 30000 } },
})

const theme = createTheme({
  palette: {
    primary: { main: '#2563eb' },
    secondary: { main: '#64748b' },
    error: { main: '#ef4444' },
  },
  typography: {
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
  },
  shape: { borderRadius: 10 },
})

export default function App() {
  return (
    <Provider store={store}>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <BrowserRouter>
            <AuthInitializer />
            <Routes>
              <Route element={<Layout />}>
                <Route path="/" element={<HomePage />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />
                <Route path="/oauth/callback" element={<OAuthCallbackPage />} />
                <Route path="/trips" element={<ProtectedRoute><TripsPage /></ProtectedRoute>} />
                <Route path="/trips/new" element={<ProtectedRoute><CreateTripPage /></ProtectedRoute>} />
                <Route path="/trips/:id" element={<ProtectedRoute><TripDetailPage /></ProtectedRoute>} />
                <Route path="/bookings" element={<ProtectedRoute><BookingsPage /></ProtectedRoute>} />
                <Route path="/payments" element={<ProtectedRoute><PaymentsPage /></ProtectedRoute>} />
                <Route path="/accommodations" element={<ProtectedRoute><AccommodationsPage /></ProtectedRoute>} />
                <Route path="/subscription" element={<ProtectedRoute><SubscriptionPage /></ProtectedRoute>} />
                <Route path="/feed" element={<ProtectedRoute><FeedPage /></ProtectedRoute>} />
                <Route path="/chat" element={<ProtectedRoute><ChatPage /></ProtectedRoute>} />
                <Route path="/chat/:roomId" element={<ProtectedRoute><ChatRoomPage /></ProtectedRoute>} />
                <Route path="/notifications" element={<ProtectedRoute><NotificationsPage /></ProtectedRoute>} />
                <Route path="/streaming" element={<ProtectedRoute><LiveStreamsPage /></ProtectedRoute>} />
              </Route>
            </Routes>
          </BrowserRouter>
        </ThemeProvider>
      </QueryClientProvider>
    </Provider>
  )
}
