# yml_comparer

YML Comparer

This is the java implementation of comparsion of YML offers blocks

Usage: `yml_comparer <old_file.yml> <new_file.yml>`

Result consists of lines of
`<OfferId><r><n><m><p>`
where
- `OfferId` is id of the offer
- `r` signals that offer was removed in `<new_file.yml>`
- `n` signals that offer was added in `<new_file.yml>`
- `m` signals that offer was modified in `<new_file.yml>`
- `p` signals that one or more pictures in offer probably unavailable
