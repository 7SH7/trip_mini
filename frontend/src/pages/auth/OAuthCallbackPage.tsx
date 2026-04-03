import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Box, CircularProgress, Typography, Alert } from '@mui/material'
import { useAppDispatch } from '../../store/hooks'
import { login } from '../../store/authSlice'
import { authApi } from '../../api/auth'

export default function OAuthCallbackPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const [error, setError] = useState('')

  useEffect(() => {
    const code = searchParams.get('code')
    const provider = searchParams.get('provider') || localStorage.getItem('oauth_provider')

    if (!code) {
      setError('Authorization code not found')
      return
    }

    const redirectUri = `${window.location.origin}/oauth/callback`
    const request = { code, redirectUri }

    const apiCall = provider === 'kakao'
      ? authApi.kakaoLogin(request)
      : authApi.googleLogin(request)

    apiCall
      .then(res => {
        const tokens = res.data.data!
        dispatch(login(tokens))
        localStorage.removeItem('oauth_provider')
        navigate('/')
      })
      .catch(() => {
        setError('소셜 로그인에 실패했습니다.')
      })
  }, [searchParams, navigate, dispatch])

  if (error) {
    return (
      <Box textAlign="center" py={8}>
        <Alert severity="error" sx={{ maxWidth: 400, mx: 'auto' }}>{error}</Alert>
      </Box>
    )
  }

  return (
    <Box textAlign="center" py={8}>
      <CircularProgress />
      <Typography sx={{ mt: 2 }}>로그인 중...</Typography>
    </Box>
  )
}
