import { useEffect, useRef } from 'react'
import Hls from 'hls.js'
import { Box } from '@mui/material'

interface HlsPlayerProps {
  src: string
  autoPlay?: boolean
}

export default function HlsPlayer({ src, autoPlay = true }: HlsPlayerProps) {
  const videoRef = useRef<HTMLVideoElement>(null)

  useEffect(() => {
    const video = videoRef.current
    if (!video || !src) return

    if (Hls.isSupported()) {
      const hls = new Hls()
      hls.loadSource(src)
      hls.attachMedia(video)
      hls.on(Hls.Events.MANIFEST_PARSED, () => {
        if (autoPlay) video.play()
      })
      return () => { hls.destroy() }
    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
      video.src = src
      if (autoPlay) video.play()
    }
  }, [src, autoPlay])

  return (
    <Box sx={{ width: '100%', borderRadius: 2, overflow: 'hidden', bgcolor: '#000' }}>
      <video ref={videoRef} controls style={{ width: '100%', display: 'block' }} />
    </Box>
  )
}
