import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class CopyDel {
    static class CreateCopyVisitor implements FileVisitor <Path> {
        private Path source;
        private Path destination;

        public CreateCopyVisitor(Path source, Path destination) {
            this.source = source;
            this.destination = destination;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            // before visiting entries in a directory we create the directory
            // (okay if directory already exists).

            Path newdir = destination.resolve(source.relativize(dir));
            if (!Files.exists(newdir)) {
                try {
                    Files.createDirectory(newdir);
                } catch (IOException x) {
                    System.err.format("Unable to create: %s: %s%n", newdir, x);
                    return SKIP_SUBTREE;
                }
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            copyFile(file, destination.resolve(source.relativize(file)));
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemException) {
                System.err.println("detected: " + file);
            } else {
                System.err.format("Unable to copy: %s: %s%n", file, exc);
            }
            return CONTINUE;
        }

        static void copyFile(Path source, Path target) {

            try {
                if (Files.notExists(target) || (Files.size(source) != Files.size(target))) {
                    try {
                        Files.copy(source, target, REPLACE_EXISTING);
                    } catch (IOException x) {
                        System.err.format("Unable to copy: %s: %s%n", source, x);
                    }
                }
            } catch (IOException e) {
                System.err.format("Unable to copy: %s: %s%n", source, e);
            }
        }

    }

    static class DelVisitor implements FileVisitor <Path> {
        private Path source;
        private Path destination;

        public DelVisitor(Path source, Path destination) {
            this.source = source;
            this.destination = destination;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            deleteFile(file, destination.resolve(source.relativize(file)));
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemException) {
                System.err.println("detected: " + file);
            } else {
                System.err.format("Unable to delete: %s: %s%n", file, exc);
            }
            return CONTINUE;
        }


        static void deleteFile(Path source, Path target) {
            try {
                if (Files.exists(source) && Files.notExists(target))
                    Files.delete(source);
            } catch (IOException x) {
                System.err.format("Unable to delete: %s: %s%n", source, x);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Path src = Paths.get("d:\\source");
        Path dst = Paths.get("d:\\destination");

        CreateCopyVisitor ccv = new CreateCopyVisitor(src, dst);
        Files.walkFileTree(src, ccv);

        DelVisitor cdv = new DelVisitor(dst, src);
        Files.walkFileTree(dst, cdv);
    }
}
