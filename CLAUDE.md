## Comment & Javadoc style (binding)

House style is **Oracle's *How to Write Doc Comments for the Javadoc Tool* + Google Java Style §7** for
structure, plus **ASD-STE100's Writing Rules** (the rules only, *not* its ~900-word approved dictionary,
which would reject `pollinate`, `karyotype`, etc.) for diction. Rules below are derived from measuring the
existing corpus; match it, don't improve on it.

**Javadoc**

- **Do not add any Javadocs for code not in an api package**. Code in API classes needs documentation for consumers - internal Forestry implementation code for developersode does not.
- Wrap at 120 columns. Existing `api/` averages ~49 columns per line, so err short.
- `@return` and `@param` are **noun-phrase fragments with no terminal period**. `@param` descriptions start
  with `The` unless that is genuinely wrong. Keep `@return` to one line where possible.
- Method descriptions are third-person declarative: `Used to ...`, `Sets ...`, `Determines ...`,
  `Registers ...`, `Called when ...`, `Adds ...`. Reuse the **identical** opening verb across parallel
  members. Never vary a verb for rhythm or to avoid repetition. This is the single most important rule here.
- One idea per chunk. Separate chunks with a bare `*` line; use `<p>` only for a true second paragraph.
- Examples use the `Ex.` marker and nothing else: `Ex. "species_type.forestry.bee" -> "Bee"`,
  `(ex. decaying leaves)`.

**Inline comments**

- Comments are intended to be read alongside the code. **Comments that describe what the code does should be dropped**. Avoid comments unless there's reasoning that isn't obvious from reading the surrounding code.
- Fragments, terse. Median length in this repo is ~34 chars. State just enough, then stop.
- **No terminal period.** Comments should only be a single sentence/fragment in all but the most extreme cases.
- Sentence case for new comments is lowercase.
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
