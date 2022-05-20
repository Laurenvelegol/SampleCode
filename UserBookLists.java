package com.example.goodreads;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class UserBookLists {

    private String baseURL;
    private String bookName;
    private String synopsis;
    private Document currentDoc;
    private ArrayList<String> userBookListLinks;
    private TreeMap<String, String> bookReviews;
    private TreeMap<String, Double> synopsisSimilarity = new TreeMap<String, Double>();

    public UserBookLists(String bookName) throws IOException {
        bookReviews = new TreeMap<>();
        this.baseURL = "https://www.goodreads.com/";
        this.bookName = bookName;
        try {
            this.currentDoc = Jsoup.connect(this.baseURL).get();
        } catch (IOException e) {
            System.out.println("Exception");
        }
        this.synopsis = getSynopsis(goToABook(bookName));
        getUserLinks();
    }

    //Call this method to get a link like this:
    // https://www.goodreads.com/review/list/111921-madeline?shelf=kids-and-young-adult
    public ArrayList<String> getUserBookListLinks() {
        return userBookListLinks;
    }

    public String goToABook(String bookName) {

        //How the URL looks when searching for a book
        //https://www.goodreads.com/search?q=harry+potter&qid=books

        //Split up the book into its words
        String[] titleSplit = bookName.split(" ");
        String bookNameSearch = "";
        for (String s: titleSplit) {
            bookNameSearch += s + "+";
        }

        //Search for the book based on the goodreads searching technique
        String url = this.baseURL + "search?q=" + bookNameSearch + "&qid=books";

        //Connect to the search page
        try {
            this.currentDoc = Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println("Exception 0");
        }

        //Find the first element in the search page (assumed to be the best search)
        //and go to that book's page
        Elements elements = this.currentDoc.select("a.bookTitle[href]");
        String goTo = elements.first().attr("abs:href");

        //Connect to the book page
        try {
            this.currentDoc = Jsoup.connect(goTo).get();
            return goTo;
        } catch (IOException e) {
            System.out.println("Exception 0");
            return null;
        }
    }

    //Fills the url links list (of pages of other users who enjoy the current book)
    public void getUserLinks() {

        //span.staticStars
        //"really liked it" = 4 stars
        //"it was amazing" = 5 stars

        Elements elements = this.currentDoc.select("div.reviewHeader");
        int i = 0;
        ArrayList<Element> reviewList = new ArrayList<Element>();
        for (Element e: elements) {
            if (i < 5) {
                elements = e.select("span.staticStars");

                if (elements.attr("title").equals("it was amazing")) {
                    reviewList.add(e);
                    i++;
                }
            }
            else {
                break;
            }
        }

        ArrayList<String> links = new ArrayList<String>();
        int k = 0;
        for (Element e: reviewList) {
            elements = e.select("a.actionLinkLite");
            for (Element e1: elements) {
                if (k < 10) {
                    links.add(e1.attr("abs:href"));
                }
            }

        }
        //System.out.println(links);
        userBookListLinks = links;

        //a.actionLinkLite
    }

    //iterate through related book links on shelf
    public void linkIterator(ArrayList<String> links) throws IOException {
        for (String s: links) {
            if (bookReviews.size() < 25) {
                getReviews(s);
            }
            else {
                for (Map.Entry<String, String> entry : bookReviews.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    System.out.println(key + ":" + value);
                }
                return;
            }
        }
        /*if (links.size() > 5) {
            for (int i = 0; i < 6; i++) {
                if (bookReviews.size() < 25) {
                    getReviews(links.get(i));
                }
            }
        }
        else {
            for (String s : links) {
                getReviews(s);
            }
        } */
        for (Map.Entry<String, String> entry : bookReviews.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println(key + ":" + value);
        }
    }

    //get reviews from a shelf link and add to bookReviews TreeMap
    public void getReviews(String shelfLink) throws IOException {
        Document doc = Jsoup.connect(shelfLink).get();
        System.out.println(shelfLink);
        int counter = 0;
        Elements shelf = doc.select("div.js-dataTooltip");
        if (shelf.size() > 0) {
            Element shelfselect = shelf.get(0);
            //System.out.println("success");
            Elements books = shelfselect.select("tr.bookalike.review");
            for (Element b : books) {
                if (counter < 5) {
                    Element t = b.selectFirst("a[title]");
                    String title = t.text();
                    //System.out.println(title);
                    Element r = b.selectFirst("td.field.actions");
                    Element rl = r.selectFirst("a");
                    //only get books that have text reviews - the ones that say view(with text)
                    if (rl.toString().contains("(with text)")) {
                        String link = rl.attr("href");
                        String reviewLink = "https://www.goodreads.com/" + link;
                        System.out.println("Review link is" + reviewLink);
                        try {
                            String review = getReviewText(reviewLink);
                            if (filterbyStars(reviewLink) && !title.equals(bookName)) {
                                System.out.println("adding to review");
                                bookReviews.put(title, review);
                                counter++;
                            }
                        } catch(Exception e) {
                            System.out.println("timed out, moving on");
                        }

                    }

                }
                else{
                    break;
                }

            }
        }
    }

    // get synopsis from a book
    public String getSynopsis(String bookLink) throws IOException {
        Document doc = Jsoup.connect(bookLink).get();
        String synopsis = "";
        Elements s1 = doc.select("div#description.readable.stacked");
        if (s1.size() > 0) {
            Element s = s1.get(0);
            synopsis = s.text();
            return synopsis;
        }
        return "";
    }

    // get cosine similarity
    public void findSimilarity() throws IOException {
        System.out.println("Book reviews " + bookReviews);
        TreeMap<String, Document1> nameSynopsis = new TreeMap<>();
        ArrayList<Document1> documents = new ArrayList<Document1>();
        Document1 s = new Document1(this.synopsis);
        documents.add(s);

        for (String bookName : bookReviews.keySet()) {
            String bookLink = goToABook(bookName);
            if (!getSynopsis(bookLink).toString().isEmpty()) {
                Document1 currDoc = new Document1(getSynopsis(bookLink));
                documents.add(currDoc);
                nameSynopsis.put(bookName, currDoc);
            }
        }

        Corpus corpus = new Corpus(documents);
        VectorSpaceModel vectorSpace = new VectorSpaceModel(corpus);


        for (Map.Entry<String, Document1> name : nameSynopsis.entrySet()) {
            String key = name.getKey();
            Document1 value = name.getValue();
            //System.out.println("cosine similarity is " + vectorSpace.cosineSimilarity(s, value));
            synopsisSimilarity.put(key, vectorSpace.cosineSimilarity(s, value));
        }
        System.out.println(synopsisSimilarity);
    }

    public TreeMap<String, Double> reviewSimilarity () {
        ArrayList<String> keywords = new ArrayList<String>();
        keywords.add("best");
        TreeMap<String, Double> termFrequencyReviews = new TreeMap<String, Double>();
        for (Map.Entry<String, String> e: bookReviews.entrySet()) {
            Double termFrequency = 0.0;
            String review = e.getValue();
            String bookName = e.getKey();
            for (String s: keywords) {
                if (review.contains(s)) {
                    termFrequency++;
                }
            }
            termFrequencyReviews.put(bookName, termFrequency/10);
        }
        //System.out.println(termFrequencyReviews);
        return termFrequencyReviews;
    }

    public TreeMap<Double, String> combineMaps() {

        TreeMap<String, Double> reviewSimilarityList = reviewSimilarity();
        try{
            findSimilarity();
        } catch(IOException e) {

        }
        TreeMap<Double, String> termFrequencyReviews = new TreeMap<Double, String>();
        for (Map.Entry<String, Double> e: synopsisSimilarity.entrySet()) {
            Double termFrequency = 0.0;
            String bookName = e.getKey();
            Double  reviewSimilarity = reviewSimilarityList.get(bookName);
            Double synopsisSimilarityValue  = e.getValue();
            //Algorithm: 0.3(synopsisSimilarity) + 0.7(reviewSimilarity)
            Double totalSimilarity = (synopsisSimilarityValue * 0.3) + (reviewSimilarity * 0.7);
            termFrequencyReviews.put(totalSimilarity, bookName);
        }
        System.out.println(termFrequencyReviews);
        return termFrequencyReviews;
    }


    public void rank() {
        //ArrayList<Double> similarities = (ArrayList<Double>) synopsisSimilarity.values();
        //Collections.sort(similarities);
        int j = 0;
        TreeMap<Double, String> finalList = new TreeMap<Double, String>(Collections.reverseOrder());
        for (Map.Entry<Double, String> e: combineMaps().entrySet()) {
            finalList.put(e.getKey(), e.getValue());
        }

        Set s = finalList.entrySet();
        Iterator i = s.iterator();

        // Traverse map and print elements
        while (i.hasNext() && j < 5) {
            Map.Entry e = (Map.Entry)i.next();
            System.out.print(e.getKey() + ": ");
            System.out.println(e.getValue());
            j++;
        }
    }


    //gets the text of the user review
    public String getReviewText(String reviewLink) throws IOException {
        Document doc = Jsoup.connect(reviewLink).get();
        String review = "";
        Element r = doc.select("div.reviewText.mediumText.description.readable").get(0);
        review = r.text();
        //System.out.println(review);
        return review;
    }

    //filters reviews by 5 stars
    public boolean filterbyStars(String reviewLink) throws IOException {
        Document doc = Jsoup.connect(reviewLink).get();
        boolean fiveStar = false;
        Element r = doc.select("span.staticStars.notranslate").get(0);
        String rating = r.text();
        if (rating.contains("amazing")) {
            fiveStar = true;
        }
        return fiveStar;
    }

    public static void main(String[] args) throws IOException {
        UserBookLists b = new UserBookLists("The Hunger Games");
        ArrayList<String> linksList = b.getUserBookListLinks();
        for(String s : linksList) {
            System.out.println(s);
        }
        b.linkIterator(linksList);
        b.rank();
    }
}