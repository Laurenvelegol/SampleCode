package com.example.goodreads;

import com.example.goodreads.UserBookLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class Main {

    /*
    Basically all you need to do to use the class is
    create a new “UserBookLists” object and pass in a string book name.
    Then you can call the “getUserBookListLinks()” method which will return an ArrayList of links.
    These links take you to a page of books with similar topics!
    If you click with “view(with text)”
     link for any book on that page
      it will take you to the review of the book :)
     */

    public static void main (String [] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        UserBookLists book = new UserBookLists("The Giver");
        ArrayList<String> linksList = book.getUserBookListLinks();
        for(String s : linksList) {
            System.out.println(s);
        }
        book.linkIterator(linksList);

/*
        System.out.println("Welcome to the GoodReads Book Recommendation Algorithm.\n" +
                "Do you really want to read through a bunch of reviews on GoodReads to " +
                "find another book to read?\nWe didn't think so! With this software we will " +
                "do all the hard work for you.\nGive us the title of a book you enjoyed recently " +
                "and we will do all the hard work.\nWe will find GoodReads users who enjoyed your book" +
                "and look at what other books they enjoyed in the same genre.\nThen we will parse through " +
                "their reviews of other books and use term-frequency analysis and document search to find \n" +
                "the reviews that indicate this book is most similar to your book. We will rank these to find " +
                "which books are most similar to yours.\nHappy reading!");
        System.out.println("Enter a book title: ");
        String title = sc.nextLine();
        sc.close();
        System.out.println("Getting genre...");
        UserBookLists book = new UserBookLists(title);
        System.out.println("Getting reviews...");
        ArrayList<String> linksList = book.getUserBookListLinks();
        System.out.println("Parsing reviews...");
        book.linkIterator(linksList);
 */
    }
}
