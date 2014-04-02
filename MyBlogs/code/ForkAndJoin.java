package concurrency.forkandjoinexample;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkAndJoin {

    private static final String       SEARCH_STRING = "m";
    private static final String       SEARCH_FOLDER = "/home/mike/Documents"
                                                      + "/MyStudy/MyStudy/MyBlogs/";

    private static final ForkJoinPool forkJoinPool  = new ForkJoinPool();

    public static void main(String[] args) throws IOException {
        Folder folder =
                Folder.fromDirectory(new File(SEARCH_FOLDER));
        System.out.println(countOccurrencesOnSingleThread(folder, SEARCH_STRING));
    }

    static Long countOccurrencesInParallel(Folder folder, String searchedWord) {
        return forkJoinPool.invoke(new FolderSearchTask(folder, searchedWord));
    }

    static Long countOccurrencesOnSingleThread(Folder folder, String searchedWord) {
        long count = 0;
        for (Folder subFolder : folder.getSubFolders()) {
            count = count + countOccurrencesOnSingleThread(subFolder, searchedWord);
        }
        for (Document document : folder.getDocuments()) {
            count = count + WordCounter.occurrencesCount(document, searchedWord);
        }
        return count;
    }

    static class Document {
        private final List<String> lines;

        Document(List<String> lines) {
            this.lines = lines;
        }

        List<String> getLines() {
            return this.lines;
        }

        public static Document fromFile(File file) throws IOException {
            List<String> lines = new LinkedList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                while (line != null) {
                    lines.add(line);
                    line = reader.readLine();
                }
            }
            return new Document(lines);
        }
    }

    static class Folder {
        private final List<Folder>   subFolders;
        private final List<Document> documents;

        Folder(List<Folder> subFolders, List<Document> documents) {
            this.subFolders = subFolders;
            this.documents = documents;
        }

        List<Folder> getSubFolders() {
            return this.subFolders;
        }

        List<Document> getDocuments() {
            return this.documents;
        }

        public static Folder fromDirectory(File dir) throws IOException {
            List<Document> documents = new LinkedList<>();
            List<Folder> subFolders = new LinkedList<>();
            for (File entry : dir.listFiles()) {
                if (entry.isDirectory()) {
                    subFolders.add(Folder.fromDirectory(entry));
                } else {
                    documents.add(Document.fromFile(entry));
                }
            }
            return new Folder(subFolders, documents);
        }
    }

    static class WordCounter {

        public static String[] wordsIn(String line) {
            return line.trim().split("(\\s|\\p{Punct})+");
        }

        public static Long occurrencesCount(Document document, String searchedWord) {
            long count = 0;
            for (String line : document.getLines()) {
                for (String word : wordsIn(line)) {
                    if (searchedWord.equals(word)) {
                        count = count + 1;
                    }
                }
            }
            return count;
        }
    }

    static class DocumentSearchTask extends RecursiveTask<Long> {

        private static final long serialVersionUID = 11231232131233L;
        private final Document    document;
        private final String      searchedWord;

        DocumentSearchTask(Document document, String searchedWord) {
            super();
            this.document = document;
            this.searchedWord = searchedWord;
        }

        @Override
        protected Long compute() {
            return WordCounter.occurrencesCount(document, searchedWord);
        }
    }

    static class FolderSearchTask extends RecursiveTask<Long> {
        private static final long serialVersionUID = 1213123123123L;
        private final Folder      folder;
        private final String      searchedWord;

        FolderSearchTask(Folder folder, String searchedWord) {
            super();
            this.folder = folder;
            this.searchedWord = searchedWord;
        }

        @Override
        protected Long compute() {
            long count = 0L;
            List<RecursiveTask<Long>> forks = new LinkedList<>();
            for (Folder subFolder : folder.getSubFolders()) {
                FolderSearchTask task = new FolderSearchTask(subFolder, searchedWord);
                forks.add(task);
                task.fork();
            }
            for (Document document : folder.getDocuments()) {
                DocumentSearchTask task = new DocumentSearchTask(document, searchedWord);
                forks.add(task);
                task.fork();
            }
            for (RecursiveTask<Long> task : forks) {
                count = count + task.join();
            }
            return count;
        }
    }

}
