import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom'
import {
  AppBar, Toolbar, Button, Box, Typography, Container, Badge, IconButton,
  Drawer, List, ListItemButton, ListItemIcon, ListItemText, Avatar, Divider,
  Stack, useMediaQuery, useTheme, Tooltip
} from '@mui/material'
import {
  Flight, Chat, Feed, Hotel, Subscriptions, LiveTv, Notifications,
  Menu as MenuIcon, Logout, Close, TravelExplore
} from '@mui/icons-material'
import { useAppDispatch, useAppSelector } from '../../store/hooks'
import { logout } from '../../store/authSlice'
import { useQuery } from '@tanstack/react-query'
import { notificationApi } from '../../api/notifications'
import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import styled from '@emotion/styled'

const DRAWER_WIDTH = 264

const GlassAppBar = styled(AppBar)`
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  background: rgba(255, 255, 255, 0.72) !important;
  border-bottom: 1px solid rgba(226, 232, 240, 0.8);
`

const LogoGradient = styled(Typography)`
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  font-weight: 800;
`

const navItems = [
  { label: '내 여행', path: '/trips', icon: <Flight /> },
  { label: '숙소 검색', path: '/accommodations', icon: <Hotel /> },
  { label: '피드', path: '/feed', icon: <Feed /> },
  { label: '채팅', path: '/chat', icon: <Chat /> },
  { label: '라이브', path: '/streaming', icon: <LiveTv /> },
  { label: '스트리밍 크레딧', path: '/subscription', icon: <Subscriptions /> },
]

export default function Layout() {
  const { isAuthenticated, user } = useAppSelector((state) => state.auth)
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const location = useLocation()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const [drawerOpen, setDrawerOpen] = useState(false)

  const { data: unreadCount } = useQuery({
    queryKey: ['notifications', 'unread'],
    queryFn: () => notificationApi.getUnreadCount().then(r => r.data.data?.count ?? 0),
    enabled: isAuthenticated,
    refetchInterval: 30000,
  })

  const handleLogout = () => {
    dispatch(logout())
    navigate('/login')
  }

  const drawerContent = (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Box sx={{ p: 2.5, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Stack direction="row" spacing={1.5} alignItems="center" component={Link} to="/">
          <Box sx={{
            width: 38, height: 38, borderRadius: '12px',
            background: 'linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: '0 4px 12px rgba(59,130,246,0.3)',
          }}>
            <TravelExplore sx={{ color: 'white', fontSize: 20 }} />
          </Box>
          <LogoGradient variant="h6">Trip</LogoGradient>
        </Stack>
        {isMobile && (
          <IconButton onClick={() => setDrawerOpen(false)} size="small"><Close /></IconButton>
        )}
      </Box>
      <Divider sx={{ opacity: 0.6 }} />

      <List sx={{ flex: 1, px: 1.5, py: 1.5 }}>
        {navItems.map((item) => {
          const isActive = location.pathname === item.path || location.pathname.startsWith(item.path + '/')
          return (
            <ListItemButton
              key={item.path}
              component={Link}
              to={item.path}
              onClick={() => isMobile && setDrawerOpen(false)}
              sx={{
                borderRadius: '12px', mb: 0.5, py: 1.2, px: 2,
                bgcolor: isActive ? 'primary.main' : 'transparent',
                color: isActive ? 'white' : '#64748b',
                boxShadow: isActive ? '0 4px 12px rgba(59,130,246,0.3)' : 'none',
                transition: 'all 0.2s cubic-bezier(0.25, 0.1, 0.25, 1)',
                '&:hover': { bgcolor: isActive ? 'primary.dark' : '#f1f5f9', transform: 'translateX(4px)' },
                '& .MuiListItemIcon-root': { color: isActive ? 'white' : '#94a3b8', minWidth: 40, transition: 'color 0.2s' },
              }}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} primaryTypographyProps={{ fontWeight: isActive ? 600 : 500, fontSize: '0.9rem' }} />
              {isActive && <Box sx={{ width: 6, height: 6, borderRadius: '50%', bgcolor: 'rgba(255,255,255,0.7)' }} />}
            </ListItemButton>
          )
        })}
      </List>

      <Divider sx={{ opacity: 0.6 }} />
      <Box sx={{ p: 2 }}>
        <Box sx={{
          p: 2, borderRadius: '14px', mb: 1.5,
          background: 'linear-gradient(135deg, #f8fafc, #f1f5f9)',
        }}>
          <Stack direction="row" spacing={1.5} alignItems="center">
            <Avatar sx={{
              width: 38, height: 38, fontSize: 14, fontWeight: 700,
              background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)',
            }}>
              {user?.name?.charAt(0) || 'U'}
            </Avatar>
            <Box sx={{ minWidth: 0 }}>
              <Typography variant="body2" fontWeight={600} noWrap>{user?.name || 'User'}</Typography>
              <Typography variant="caption" color="text.secondary" noWrap sx={{ fontSize: '0.7rem' }}>{user?.email || ''}</Typography>
            </Box>
          </Stack>
        </Box>
        <Button fullWidth variant="outlined" color="inherit" startIcon={<Logout />} onClick={handleLogout}
          sx={{
            borderColor: '#e2e8f0', color: '#94a3b8', borderRadius: '12px',
            transition: 'all 0.2s',
            '&:hover': { bgcolor: '#fef2f2', borderColor: '#fca5a5', color: '#ef4444' },
          }}>
          로그아웃
        </Button>
      </Box>
    </Box>
  )

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      {isAuthenticated && (
        <>
          {!isMobile && (
            <Drawer variant="permanent" sx={{
              width: DRAWER_WIDTH, flexShrink: 0,
              '& .MuiDrawer-paper': {
                width: DRAWER_WIDTH, borderRight: 'none', bgcolor: 'white',
                boxShadow: '1px 0 20px rgba(0,0,0,0.03)',
              },
            }}>
              {drawerContent}
            </Drawer>
          )}
          {isMobile && (
            <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)} sx={{
              '& .MuiDrawer-paper': { width: DRAWER_WIDTH, borderRadius: '0 20px 20px 0' },
            }}>
              {drawerContent}
            </Drawer>
          )}
        </>
      )}

      <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        <GlassAppBar position="sticky" elevation={0}>
          <Toolbar sx={{ justifyContent: 'space-between' }}>
            <Stack direction="row" spacing={1} alignItems="center">
              {isAuthenticated && isMobile && (
                <IconButton onClick={() => setDrawerOpen(true)}><MenuIcon /></IconButton>
              )}
              {!isAuthenticated && (
                <Stack direction="row" spacing={1.5} alignItems="center" component={Link} to="/">
                  <Box sx={{
                    width: 34, height: 34, borderRadius: '10px',
                    background: 'linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    boxShadow: '0 4px 12px rgba(59,130,246,0.3)',
                  }}>
                    <TravelExplore sx={{ color: 'white', fontSize: 18 }} />
                  </Box>
                  <LogoGradient variant="h6">Trip</LogoGradient>
                </Stack>
              )}
            </Stack>

            <Stack direction="row" spacing={1} alignItems="center">
              {isAuthenticated ? (
                <>
                  <Tooltip title="알림">
                    <IconButton component={Link} to="/notifications" sx={{
                      transition: 'all 0.2s',
                      '&:hover': { bgcolor: '#eff6ff' },
                    }}>
                      <Badge badgeContent={unreadCount || 0} color="error"
                        sx={{ '& .MuiBadge-badge': { fontWeight: 700, fontSize: '0.7rem' } }}>
                        <Notifications sx={{ color: location.pathname === '/notifications' ? 'primary.main' : '#94a3b8' }} />
                      </Badge>
                    </IconButton>
                  </Tooltip>
                </>
              ) : (
                <>
                  <Button component={Link} to="/login" sx={{ color: '#475569', fontWeight: 500 }}>로그인</Button>
                  <Button component={Link} to="/register" variant="contained" disableElevation
                    sx={{ background: 'linear-gradient(135deg, #3b82f6, #8b5cf6)', borderRadius: '10px' }}>
                    회원가입
                  </Button>
                </>
              )}
            </Stack>
          </Toolbar>
        </GlassAppBar>

        <Box sx={{ flex: 1 }}>
          <Container maxWidth="lg" sx={{ py: 4 }}>
            <AnimatePresence mode="wait">
              <motion.div
                key={location.pathname}
                initial={{ opacity: 0, y: 8 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -8 }}
                transition={{ duration: 0.25, ease: [0.25, 0.1, 0.25, 1] as const }}
              >
                <Outlet />
              </motion.div>
            </AnimatePresence>
          </Container>
        </Box>
      </Box>
    </Box>
  )
}
