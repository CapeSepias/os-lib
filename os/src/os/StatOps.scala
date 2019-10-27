package os

import java.nio.file.{Files, LinkOption}
import java.nio.file.attribute._

import scala.util.Try


/**
  * Checks whether the given path is a symbolic link
  */
object isLink extends Function1[Path, Boolean]{
  def apply(p: Path): Boolean = Files.isSymbolicLink(p.wrapped)
}

/**
  * Checks whether the given path is a regular file
  */
object isFile extends Function1[Path, Boolean]{
  def apply(p: Path): Boolean = Files.isRegularFile(p.wrapped)
  def apply(p: Path, followLinks: Boolean = true): Boolean = {
    val opts = if (followLinks) Array[LinkOption]() else Array(LinkOption.NOFOLLOW_LINKS)
    Files.isRegularFile(p.wrapped, opts:_*)
  }
}


/**
  * Checks whether the given path is a directory
  */
object isDir extends Function1[Path, Boolean]{
  def apply(p: Path): Boolean = Files.isDirectory(p.wrapped)
  def apply(p: Path, followLinks: Boolean = true): Boolean = {
    val opts = if (followLinks) Array[LinkOption]() else Array(LinkOption.NOFOLLOW_LINKS)
    Files.isDirectory(p.wrapped, opts:_*)
  }
}

/**
  * Gets the size of the given file
  */
object size extends Function1[Path, Long]{
  def apply(p: Path): Long = Files.size(p.wrapped)
}

/**
  * Gets the mtime of the given file
  */
object mtime extends Function1[Path, Long]{
  def apply(p: Path): Long = Files.getLastModifiedTime(p.wrapped).toMillis
  def apply(p: Path, followLinks: Boolean = true): Long = {
    val opts = if (followLinks) Array[LinkOption]() else Array(LinkOption.NOFOLLOW_LINKS)
    Files.getLastModifiedTime(p.wrapped, opts:_*).toMillis
  }

  /**
    * Sets the mtime of the given file.
    *
    * Note that this always follows links to set the mtime of the referred-to file.
    * Unfortunately there is no Java API to set the mtime of the link itself:
    *
    * https://stackoverflow.com/questions/17308363/symlink-lastmodifiedtime-in-java-1-7
    */
  object set {
    def apply(p: Path, millis: Long) = {
      Files.setLastModifiedTime(p.wrapped, FileTime.fromMillis(millis))
    }
  }
}

/**
  * Reads in the basic filesystem metadata for the given file. By default follows
  * symbolic links to read the metadata of whatever the link is pointing at; set
  * `followLinks = false` to disable that and instead read the metadata of the
  * symbolic link itself.
  */
object stat extends Function1[os.Path, os.StatInfo]{
  def apply(p: os.Path): os.StatInfo = apply(p, followLinks = true)
  def apply(p: os.Path, followLinks: Boolean = true): os.StatInfo = {
    val opts = if (followLinks) Array[LinkOption]() else Array(LinkOption.NOFOLLOW_LINKS)
    os.StatInfo.make(Files.readAttributes(p.wrapped, classOf[BasicFileAttributes], opts:_*))
  }
  object posix{
    def apply(p: os.Path): os.PosixStatInfo = apply(p, followLinks = true)
    def apply(p: os.Path, followLinks: Boolean = true): os.PosixStatInfo = {
      val opts = if (followLinks) Array[LinkOption]() else Array(LinkOption.NOFOLLOW_LINKS)
      os.PosixStatInfo.make(Files.readAttributes(p.wrapped, classOf[PosixFileAttributes], opts:_*))
    }
  }
  /**
    * Reads in the full filesystem metadata for the given file. By default follows
    * symbolic links to read the metadata of whatever the link is pointing at; set
    * `followLinks = false` to disable that and instead read the metadata of the
    * symbolic link itself.
    */
  object full extends Function1[os.Path, os.FullStatInfo] {
    def apply(p: os.Path): os.FullStatInfo = apply(p, followLinks = true)
    def apply(p: os.Path, followLinks: Boolean = true): os.FullStatInfo = {
      val opts = if (followLinks) Array[LinkOption]() else Array(LinkOption.NOFOLLOW_LINKS)
      os.FullStatInfo.make(
        Files.readAttributes(
          p.wrapped,
          classOf[BasicFileAttributes],
          opts:_*
        ),
        Try(Files.readAttributes(
          p.wrapped,
          classOf[PosixFileAttributes],
          opts:_*
        )).toOption
      )
    }
  }


}
