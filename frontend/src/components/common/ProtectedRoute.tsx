import { Navigate } from 'react-router-dom'
import { useAppSelector } from '../../store/hooks'

export default function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, initialized } = useAppSelector((state) => state.auth)

  if (!initialized) return null
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <>{children}</>
}
