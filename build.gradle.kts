
import myaa.subkt.ass.*
import myaa.subkt.tasks.*
import myaa.subkt.tasks.Mux.*
import myaa.subkt.tasks.Nyaa.*
import java.awt.Color
import java.time.*

plugins {
    id("myaa.subkt")
}

subs {
    readProperties("sub.properties")
    episodes(getList("episodes"))

    merge {
        from(get("dialogue")) {
            incrementLayer(10)
        }
        from(getList("eyecatch"))
        from(getList("typesetting"))
        
        if(propertyExists("OP")) {
            from(get("OP")) {
                if(propertyExists("opsync")) {
                    syncTargetTime(getAs<Duration>("opsync"))
                }
            }
        }
        
        if(propertyExists("ED")) {
            from(get("ED")) {
                if(propertyExists("edsync")) {
                    syncTargetTime(getAs<Duration>("edsync"))
                }
            }
        }

        includeExtraData(false)
        includeProjectGarbage(false)
        
        scriptInfo {
            scaledBorderAndShadow = true
        }
    }

    val signs by task<ASS> {
        from(merge.item())
        
        ass {
            events.lines.removeIf { line ->
                line.style.contains("Default")
            }
        }
    }

    val resample by task<SubExec> {
        enabled = (!file(get("en_audio")).exists()) || (!file(get("ja_audio")).exists())

        executable("ffmpeg")
        args(
            "-y", "-i", file(get("premux")), "-vn", "-map", "0:a:m:language:eng", 
            "-af", "aresample=48000:resampler=soxr:precision=33:osf=s16:dither_method=shibata", 
            file(get("en_audio"))
        )

        executable("ffmpeg")
        args(
            "-y", "-i", file(get("premux")), "-vn", "-map", "0:a:m:language:jpn", 
            "-af", "aresample=48000:resampler=soxr:precision=33:osf=s16:dither_method=shibata", 
            file(get("ja_audio"))
        )
    }

    chapters {
        from(get("chapters"))
    }

    mux {
        dependsOn(resample.item())
        
        title(get("title"))

        from(get("premux")) {
            video {
                lang("en")
                name("BD 1080p Hi10 [Raizel]")
                trackOrder(0)
            }
            audio { include(false) }
            attachments { include(false) }
            subtitles { include(false) }
            includeChapters(false)
        }

        from(get("ja_audio")) {
            tracks {
                lang("ja")
                name("Japanese 2.0 FLAC")
                trackOrder(1)
                default(true)
            }
        }

        from(get("en_audio")) {
            tracks {
                lang("en")
                name("English 2.0 FLAC")
                trackOrder(2)
                default(true)
            }
        }

        from(merge.item()) {
            tracks {
                lang("en")
                name("Full Subtitles [Commie/ASC]")
                default(true)
                compression(CompressionType.ZLIB)
            }
        }

        from(signs.item()) {
            tracks {
                lang("en")
                name("Signs & Songs")
                default(false)
                forced(true)
                compression(CompressionType.ZLIB)
            }
        }

        chapters(chapters.item()) {
            lang("en")
        }

        attach(get("fonts")) {
            includeExtensions("ttf", "otf")
        }

        attach(get("commonfonts")) {
            includeExtensions("ttf", "otf")
        }

        skipUnusedFonts(true)
        out(get("muxfile"))
    }
}