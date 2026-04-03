import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Paper, TextField, Button, Typography, Box, Card, CardContent, CardMedia, Chip, Stack, Skeleton, InputAdornment } from '@mui/material'
import { Search, Hotel, Place, Phone } from '@mui/icons-material'
import { accommodationApi } from '../../api/accommodations'
import type { AccommodationResponse } from '../../types'

export default function AccommodationsPage() {
  const [keyword, setKeyword] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['accommodations', searchKeyword],
    queryFn: () => accommodationApi.search({ keyword: searchKeyword || undefined, size: 20 }).then(r => r.data.data),
    enabled: !!searchKeyword,
  })

  const handleSearch = () => setSearchKeyword(keyword)

  return (
    <Paper sx={{ p: 3 }} elevation={0}>
      <Typography variant="h5" fontWeight={600} gutterBottom>숙소 검색</Typography>

      <Stack direction="row" spacing={1} sx={{ mb: 3 }}>
        <TextField
          fullWidth placeholder="숙소명 또는 지역 검색"
          value={keyword} onChange={e => setKeyword(e.target.value)}
          onKeyDown={e => e.key === 'Enter' && handleSearch()}
          slotProps={{ input: { startAdornment: <InputAdornment position="start"><Search /></InputAdornment> } }}
        />
        <Button variant="contained" onClick={handleSearch} sx={{ minWidth: 100 }}>검색</Button>
      </Stack>

      {!searchKeyword && (
        <Box textAlign="center" py={5} color="#999">
          <Hotel sx={{ fontSize: 48, mb: 1 }} />
          <Typography>검색어를 입력하세요</Typography>
        </Box>
      )}

      {isLoading && (
        <Stack spacing={2}>{[1,2,3].map(i => <Skeleton key={i} height={200} variant="rounded" />)}</Stack>
      )}

      {data && (
        <Stack spacing={2}>
          {data.content?.length === 0 && (
            <Typography textAlign="center" color="#999" py={3}>검색 결과가 없습니다.</Typography>
          )}
          {data.content?.map((acc: AccommodationResponse) => (
            <Card key={acc.id} sx={{ display: 'flex' }} elevation={0} variant="outlined">
              {acc.imageUrl && (
                <CardMedia component="img" sx={{ width: 200, objectFit: 'cover' }}
                  image={acc.imageUrl} alt={acc.title} />
              )}
              <CardContent sx={{ flex: 1 }}>
                <Typography variant="h6" fontWeight={600}>{acc.title}</Typography>
                {acc.address && (
                  <Typography variant="body2" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.5 }}>
                    <Place fontSize="small" /> {acc.address}
                  </Typography>
                )}
                {acc.tel && (
                  <Typography variant="body2" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <Phone fontSize="small" /> {acc.tel}
                  </Typography>
                )}
                <Box sx={{ mt: 1 }}>
                  {acc.price && <Chip label={`${acc.price.toLocaleString()}원~`} color="primary" size="small" />}
                  {acc.category && <Chip label={acc.category} size="small" sx={{ ml: 0.5 }} />}
                </Box>
              </CardContent>
            </Card>
          ))}
        </Stack>
      )}
    </Paper>
  )
}
