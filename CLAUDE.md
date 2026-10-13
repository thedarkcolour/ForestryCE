## Comment & Javadoc style (binding)

**When to write a comment** (decide this first, the formatting rules below come second)

- **Do not add any Javadocs for code not in an api package**. Code in API classes needs documentation for
  consumers. Internal Forestry implementation code does not.
- In non-API code, a comment on a class, method, or field is a `//` comment on the line above it, and only
  when it passes the next rule.
- Comments are intended to be read alongside the code. **Comments that describe what the code does should be
  dropped**. A comment needs reasoning that isn't obvious from reading the surrounding code, which means one of:
  - an ordering constraint (`// must run before any species rebuild`)
  - why the obvious alternative is wrong (`// not ServerLifecycleHooks, the server is still null during the first load`)
  - a lifecycle or threading trap (`// a supplier because the codec needs the karyotype, which does not exist at class init`)
- **Existing comments: match them, don't improve on them.** The one exception is documentation written by
  Opus 5, which should be cut down to this style whenever its file is touched. Opus 5 documentation is
  recognizable by Javadoc on non-API code, multi-sentence `//` blocks, `<p>` paragraphs with bold lead-ins
  (ex. `<b>Ordering matters:</b>`), and prose that restates the code. Comments from original Forestry stay as
  they are.

**House style** is Oracle's *How to Write Doc Comments for the Javadoc Tool* + Google Java Style §7 for
structure. Diction follows these rules from ASD-STE100 (the rules only, *not* its ~900-word approved
dictionary, which would reject `pollinate`, `karyotype`, etc.):

- One word has one meaning. Do not use a second word for the same thing.
- Short sentences, one idea per sentence.
- Active voice.
- No noun clusters longer than three words.

The rules below are derived from measuring the existing corpus.

**Javadoc** (api packages only)

- Wrap at 120 columns. Existing `api/` averages ~49 columns per line, so err short.
- `@return` and `@param` are **noun-phrase fragments with no terminal period**. `@param` descriptions start
  with `The` unless that is genuinely wrong. Keep `@return` to one line where possible.
- Method descriptions are third-person declarative: `Used to ...`, `Sets ...`, `Determines ...`,
  `Registers ...`, `Called when ...`, `Adds ...`. Reuse the **identical** opening verb across parallel
  members. Never vary a verb for rhythm or to avoid repetition. This is the most important Javadoc rule.
- One idea per chunk. Separate chunks with a bare `*` line; use `<p>` only for a true second paragraph.
- Examples use the `Ex.` marker and nothing else: `Ex. "species_type.forestry.bee" -> "Bee"`,
  `(ex. decaying leaves)`.

**Inline comments**

- Fragments, terse. Median length in this repo is ~34 chars. State just enough, then stop.
- **No terminal period.** Comments should only be a single sentence/fragment in all but the most extreme cases.
- Start new comments with a lowercase letter.
- Established domain acronyms (`NBT`, `JEI`, `API`, `GUI`, `ID`) go in bare and unexpanded. Never coin a new abbreviation.
- Lowercase `todo` for new todo items. Any todos containing uppercase characters (ex. TODO, Todo) are assumed to be from old commits in
  original Forestry, not Forestry: CE.

**Punctuation: ASCII only.** The existing corpus contains **zero** non-ASCII characters across 5,195
comment/Javadoc lines. No em-dash, no en-dash, no curly quotes, no `…`. The only dash is hyphen-minus, and
only as a minus sign or a list separator. Recast an em-dash parenthetical as a separate sentence, a comma
pair, or parentheses; never substitute ` - ` or ` -- ` for it.

**Avoid** (these read as machine-written): hedges (`it's worth noting`, `essentially`, `simply`), adverb
padding (`seamlessly`, `robustly`, `carefully`), restating the method signature in prose, and elegant
variation across sibling members.
