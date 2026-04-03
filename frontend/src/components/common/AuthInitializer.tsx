import { useEffect } from 'react'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { initAuth } from '../../store/authSlice'

export default function AuthInitializer() {
  const dispatch = useAppDispatch()
  const { initialized } = useAppSelector((state) => state.auth)

  useEffect(() => {
    if (!initialized) {
      dispatch(initAuth())
    }
  }, [dispatch, initialized])

  return null
}
