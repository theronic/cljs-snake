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
- Turn left/right with the arrow keys
- Take the red pill
- Die by running into your tail
- Get a high score

## Turning

When turning, the velocity vector is rotated by 90 degrees clockwise or counter-clockwise in the `next-state` function using a hard-coded [rotation matrix](https://en.wikipedia.org/wiki/Rotation_matrix).

## Data Model

The simulation state is stored in an atom named `state` with these keys:

- `:size` stores the size of the body of the snake, initially 3.
- `:position` stores the snake's head as an `[x y]` coordinate, e.g. `[7 9]`
- `:history` stores a list of previous head positions, so we can render the snake's body as the last N head positions, e.g. `([7 9] [6 9] [6 8] ...)`.
- `:velocity` holds a relative `[x y]` vector that is added to the `:position` coordinate on every animation tick.
- `:dead?` will be true when the snake die by running into its own body.
- `:pills` stores a set of edible `[x y]` pill coordinates.
