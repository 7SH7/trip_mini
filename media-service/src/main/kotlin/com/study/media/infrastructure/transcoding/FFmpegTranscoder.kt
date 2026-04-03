package com.study.media.infrastructure.transcoding

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Component
class FFmpegTranscoder(
    @Value("\${media.ffmpeg-path}") private val ffmpegPath: String,
    @Value("\${media.temp-dir}") private val tempDir: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    data class TranscodeResult(
        val success: Boolean,
        val outputDir: String,
        val variants: List<String> = emptyList(),
        val error: String? = null
    )

    @Async
    fun transcode(inputFile: File, jobId: Long): TranscodeResult {
        val outputDir = "$tempDir/job-$jobId"
        Files.createDirectories(Path.of(outputDir))

        return try {
            val variants = listOf("720", "480", "360")

            for (resolution in variants) {
                val outputFile = "$outputDir/${resolution}p.m3u8"
                val segmentPattern = "$outputDir/${resolution}p_%03d.ts"

                val process = ProcessBuilder(
                    ffmpegPath, "-i", inputFile.absolutePath,
                    "-vf", "scale=-2:$resolution",
                    "-c:v", "libx264", "-preset", "fast",
                    "-c:a", "aac", "-b:a", "128k",
                    "-hls_time", "10",
                    "-hls_list_size", "0",
                    "-hls_segment_filename", segmentPattern,
                    "-f", "hls", outputFile
                ).redirectErrorStream(true).start()

                val exitCode = process.waitFor()
                if (exitCode != 0) {
                    val error = process.inputStream.bufferedReader().readText()
                    return TranscodeResult(false, outputDir, error = "FFmpeg failed for ${resolution}p: $error")
                }
            }

            // Create master playlist
            val masterPlaylist = buildString {
                appendLine("#EXTM3U")
                appendLine("#EXT-X-STREAM-INF:BANDWIDTH=2800000,RESOLUTION=1280x720")
                appendLine("720p.m3u8")
                appendLine("#EXT-X-STREAM-INF:BANDWIDTH=1400000,RESOLUTION=854x480")
                appendLine("480p.m3u8")
                appendLine("#EXT-X-STREAM-INF:BANDWIDTH=800000,RESOLUTION=640x360")
                appendLine("360p.m3u8")
            }
            File("$outputDir/master.m3u8").writeText(masterPlaylist)

            log.info("Transcoding completed for job {}", jobId)
            TranscodeResult(true, outputDir, variants.map { "${it}p" })
        } catch (e: Exception) {
            log.error("Transcoding failed for job {}", jobId, e)
            TranscodeResult(false, outputDir, error = e.message)
        }
    }
}
