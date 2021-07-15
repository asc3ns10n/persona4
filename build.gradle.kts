
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
    release(arg("release") ?: "TV")

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
    }

    val mergeSigns by task<Merge> {
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
    }

    chapters {
        from(get("chapters"))
    }

    mux {
        title(get("title"))

        from(get("premux")) {
            video {
                trackOrder(0)
                lang("jpn")
                name("BD 1080p Hi10 [Salender-Raws]")
            }
            audio {
                trackOrder(2)
                lang("jpn")
                name("Japanese 2.0 FLAC")
            }
            attachments { include(false) }
            subtitles { include(false) }
        }

        from(get("dub")) {
            audio {
                lang("eng")
                name("English 2.0 FLAC")
                trackOrder(1)
                default(true)
                forced(true)
            }
        }

        from(mergeSigns.item()) {
            tracks {
                name("Signs & Songs")
                lang("eng")
                default(true)
                forced(true)
            }
        }
        
        from(merge.item()) {
            tracks {
                name("Full Subtitles")
                lang("eng")
            }
        }

        chapters(chapters.item()) {
            lang("eng")
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