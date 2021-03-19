package com.github.fntz.omhs.handlers.writers

import com.github.fntz.omhs.internal.FileDef
import io.netty.channel.{ChannelFuture, ChannelFutureListener}

trait FileCleaner {
  protected def fileCleaner(files: List[FileDef]): ChannelFutureListener = {
    (_: ChannelFuture) => {
      files.flatMap(_.value).filter(_.refCnt() != 0).map(_.release())
    }
  }
}
