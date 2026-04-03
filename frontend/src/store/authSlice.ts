import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import client from '../api/client'
import type { ApiResponse, UserResponse } from '../types'

interface AuthState {
  isAuthenticated: boolean
  user: UserResponse | null
  loading: boolean
  initialized: boolean
}

const initialState: AuthState = {
  isAuthenticated: !!localStorage.getItem('accessToken'),
  user: null,
  loading: false,
  initialized: false,
}

export const initAuth = createAsyncThunk('auth/init', async (_, { rejectWithValue }) => {
  const token = localStorage.getItem('accessToken')
  if (!token) return null
  try {
    const res = await client.get<ApiResponse<UserResponse>>('/api/users/me')
    return res.data.data
  } catch {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    return rejectWithValue(null)
  }
})

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    login(state, action) {
      const { accessToken, refreshToken } = action.payload
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      state.isAuthenticated = true
    },
    logout(state) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      state.isAuthenticated = false
      state.user = null
    },
    setUser(state, action) {
      state.user = action.payload
      state.isAuthenticated = true
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(initAuth.pending, (state) => { state.loading = true })
      .addCase(initAuth.fulfilled, (state, action) => {
        state.loading = false
        state.initialized = true
        if (action.payload) {
          state.user = action.payload
          state.isAuthenticated = true
        } else {
          state.isAuthenticated = false
        }
      })
      .addCase(initAuth.rejected, (state) => {
        state.loading = false
        state.initialized = true
        state.isAuthenticated = false
        state.user = null
      })
  },
})

export const { login, logout, setUser } = authSlice.actions
export default authSlice.reducer
