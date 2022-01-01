package edu.slz.springlab.chuckjokes.controllers;

import edu.slz.springlab.chuckjokes.services.JokeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JokeController {

  private JokeService jokeService;

  @Autowired
  public JokeController(JokeService jokeService) {
    this.jokeService = jokeService;
  }

  @GetMapping({"/", ""})
  public String getJoke(Model model) {
    String joke = jokeService.getJoke();

    model.addAttribute("joke", joke);

    return "index";
  }

}
