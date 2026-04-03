import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Paper, TextField, Button, Typography, Alert, Stack, Box } from '@mui/material'
import styled from 'styled-components'
import { useAppDispatch } from '../../store/hooks'
import { login } from '../../store/authSlice'
import { authApi } from '../../api/auth'

const AuthContainer = styled(Paper)`
  max-width: 420px;
  margin: 3rem auto;
  padding: 2.5rem;
`

export default function RegisterPage() {
  const [email, setEmail] = useState('')
  const [name, setName] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const dispatch = useAppDispatch()
  const navigate = useNavigate()

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      const res = await authApi.register({ email, name, password })
      const tokens = res.data.data!
      dispatch(login(tokens))
      navigate('/')
    } catch {
      setError('회원가입에 실패했습니다.')
    }
  }

  return (
    <AuthContainer elevation={0}>
      <Typography variant="h5" fontWeight={600} gutterBottom>회원가입</Typography>
      <Box component="form" onSubmit={handleSubmit}>
        <Stack spacing={2}>
          <TextField label="이메일" type="email" value={email} onChange={e => setEmail(e.target.value)} required fullWidth />
          <TextField label="이름" value={name} onChange={e => setName(e.target.value)} required fullWidth />
          <TextField label="비밀번호" type="password" value={password} onChange={e => setPassword(e.target.value)} required fullWidth />
          {error && <Alert severity="error">{error}</Alert>}
          <Button type="submit" variant="contained" size="large" fullWidth>회원가입</Button>
        </Stack>
      </Box>
      <Typography variant="body2" textAlign="center" sx={{ mt: 2 }}>
        이미 계정이 있으신가요? <Link to="/login">로그인</Link>
      </Typography>
    </AuthContainer>
  )
}
