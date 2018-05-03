## jhtml2md
A simple converter from HTML to Markdown in Java.

I created this project for importing my blog on github markdown.

## How to use it:

It's pretty simple, first add jSoup to the classpath. Then:

String markdownText = HTML2Md.convert(html, baseURL);
Where html is a String containing the html code you want to convert, and baseURL is the url you will use as a reference for converting relative links.

You can use directly an URL too, like this:

    URL url = new URL("http://www.test.com/");
    HTML2Md.convert(url, 2000);

The 2000 is the timeout for requesting the page in milliseconds.
