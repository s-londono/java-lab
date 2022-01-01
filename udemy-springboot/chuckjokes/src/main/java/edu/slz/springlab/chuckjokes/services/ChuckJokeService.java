package edu.slz.springlab.chuckjokes.services;

import guru.springframework.norris.chuck.ChuckNorrisQuotes;
import org.springframework.stereotype.Service;

@Service
public class ChuckJokeService implements JokeService {

  private final ChuckNorrisQuotes chuckQuotes;

  public ChuckJokeService() {
    this.chuckQuotes = new ChuckNorrisQuotes();
  }

  @Override
  public String getJoke() {
    return chuckQuotes.getRandomQuote();
  }

}
