package org.rickosborne.itsmymusic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;

public class DelimitedLine {
  LinkedList<String> parts = new LinkedList<String>();
  DelimitedLine(String line, String delim) {
    parts.addAll(Arrays.asList(line.split(delim)));
  }

  public String next() {
    if (parts.isEmpty()) return null;
    String head = parts.removeFirst();
    if (head.isEmpty()) return null;
    return head;
  }

  public Integer nextInt() {
    String head = next();
    if (head == null) return null;
    try {
      return Integer.parseInt(head, 10);
    }
    catch(NumberFormatException e) {}
    return null;
  }

  public URL nextUrl() {
    String head = next();
    if (head == null) return null;
    try {
      return new URL(head);
    }
    catch (MalformedURLException e) {}
    return null;
  }
}
