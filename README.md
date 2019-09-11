# UrlScanner
 
The URL scanner finds links in a give *base path* and saves them locally.
this process is done upto a configurable depth, with a configurable limit of links per page.

Excpected Input:
 base URL - The first URL to scan for links at.
 Max number of URLs per page
 Max Depth - how deep should this process go when collecting links for every page
 Unique - set to 'true' to prevent going to the same link twice

Example: java UrlScanner https://www.ynetnews.com 5 2 true


TODO:
 * validate connectivity to URL before adding it
 * Support relative URLs
 * Test classes
