# Terminal Spelling Bee

Simple Java project for my school's AP Computer Science A class. Took about 3 days to make.

I don't really know Java that well. I am not bothering with external libraries and toolchains.

This project lets you play the [New York Times' Spelling Bee game](https://nytimes.com/puzzles/spelling-bee) in the terminal.
It works by getting the HTML file at the link above and parsing the JSON within a `<script>` tag that contains the game data (letters, valid words, etc.).
However, that file only contains game data for the past 2 weeks. Luckily, NYTimes records all of their previous games at `https://www.nytimes.com/{year}/{month}/{day}/crosswords/spelling-bee-forum.html`, but without the answers (in an easy-to-parse format, at least), so for those games, I just ask for words from a free dictionary API.

## How to use

Requires Java. And internet. That's kinda it.

In your standard Unix system, open a terminal and navigate to this directory.
Compilation and execution are handled by a neat little shell script found in this directory.
In your shell, simply run `./run` to execute my code.

If you are on Windows, idk. Manually compile each file? Unless your IDE can do something special, I guess.

Alternatively, if you wish to modify or use parts of the code: the file structure is as follows:

```tree
.
├── build
│   ├── deps
│   │   └── File.class
│   └── Main.class
├── deps
│   └── File.java
├── Main.java
├── README.md
└── run
```

You can ignore the `build` directory, that is for the `run` script.
I typically try to abstract away some of the more complex functions into files within the `deps` directory, but everything starts at `Main.java`.

## Pull Requests

Like I said, I don't know too much Java. However, this class is also acting as an introduction to programming in general, so it gets boring at times (because I *do* have a foundation in general programming). So, teach me stuff! I'm stuck with this language for about 6 more months for this class, so it would be cool to learn whatever I can. Maybe something to troll the AP graders with :)
