# Snake in 100 Lines of ClojureScript

![Snake Gameplay GIF](https://s3-eu-west-1.amazonaws.com/petrus-blog/snake-gameplay-3.gif)

On 12 August 2015, I presented a talk at UCT on [The State of the Art in Front End Development](http://petrustheron.com/posts/sota-front-end-development-clojurescript.html), during which we built a game in ClojureScript.

You can download the slides with coding videos and audio by [reading the blog post](http://petrustheron.com/posts/sota-front-end-development-clojurescript.html).

## Tools:

- ClojureScript 1.7
- Boot-clj

## To Run:

```
boot dev
```

When it's done compiling, open `http://localhost:8002/`

## Features:

You can:
- control the snake with left/right arrow keys
- take the red pill
- die by running into your tail
- get a high score

## Rotations

The only complex thing is the `multiply-matrix` function, used to rotate the `:velocity` vector by a 90 or 270-degree rotation matrix depending on if you pressed the left or right arrow key.

## Data Model

The simutation is stored in an atom named `state` with these keys:

- `:size` stores the size of the body of the snake, e.g. `3`,
- `:position` stores snake's head as an `[x y]` coordinate, e.g. `[7 9]`
- `:history` stores a list of previous head positions, so we can render the snake's body as the last N head positions, e.g. `([7 9] [6 9] [6 8] ...)`.
- `:velocity` will shift `:position` by a relative `[x y]` value on the next animation tick.
- `:dead?` did the snake run into its own body?
- `:pills` stores a set of pill coordinates.
