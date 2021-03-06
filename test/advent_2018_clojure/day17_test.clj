(ns advent-2018-clojure.day17-test
  (:require [clojure.test :refer :all]
            [advent-2018-clojure.day17 :refer :all]))

(def TEST_DATA "x=495, y=2..7\ny=7, x=495..501\nx=501, y=3..7\nx=498, y=2..4\nx=506, y=1..2\nx=498, y=10..13\nx=504, y=10..13\ny=13, x=498..504")

(def NESTED_BUCKETS "
  444455555                        444455555
  999900000                        999900000
  678901234                        678901234
0 ....+....                      0 ....+....
1 .........                      1 ....|....
2 .........     should become    2 ....|....  ignoring this line and above
3 .#.......                      3 .#|||||||
4 .#.#.#.#.                      4 .#~#~#~#|
5 .#.###.#.                      5 .#~###~#|
6 .#.....#.                      6 .#~~~~~#|
7 .#######.                      7 .#######|"
  "x=497, y=3..7\ny=7, x=498..503\nx=503, y=4..6\nx=499, y=4..5\nx=501, y=4..5\nx=500, y=5..5")

(def TOP_ROW "
  455                        455
  900                        900
  901                        901
0 .+.                      0 .+.
1 ...                      1 |||   ignoring this line and above
2 .#.     should become    2 |#|"
  "x=500, y=2..2")

(def DOUBLE_DROP "
  444455555                        444455555
  999900000                        999900000
  678901234                        678901234
0 ....+....                      0 ....+....
1 .........                      1 ...|||...   ignoring this line and above
2 ....#....     should become    2 ||||#||||
3 .#.#.#.#.                      3 |#~#.#~#|
4 .#.....#.                      4 |#~~~~~#|
5 .#######.                      5 |#######|"
  "x=500, y=2..2\nx=497, y=3..3\nx=497, y=3..5\nx=499, y=3..3\nx=501, y=3..3\nx=503, y=3..5\ny=5, x=498..502")

(def FULLY_ENCLOSED "
  445555                        445555
  990000                        990000
  890123                        890123
0 ..+...                      0 ..+...   ignoring this line and above (nothing to ignore)
1 .#..#.                      1 .#~~#.
2 ..#.#.     should become    2 ..#~#.
3 ..###.                      3 ..###."
  "x=499, y=1..1\nx=502, y=1..3\nx=500, y=2..3\nx=501, y=3..3")

(def TWO_DRIPS_TO_SAME_TARGET "
  444455555                        444455555
  999900000                        999900000
  678901234                        678901234
0 ....+....                      0 ....+....
1 .........                      1 ..|||||..   ignoring this line and above
2 ...###...     should become    2 ..|###|..
3 .........                      3 |||||||||
4 .#.....#.                      4 |#~~~~~#|
5 .#######.                      5 |#######|"
  "y=2, x=499..501\nx=497, y=4..5\nx=503, y=4..5\ny=5, x=498..502")

(def TWO_DRIPS_TO_SAME_TARGET_WITH_DIVIDER "
  444455555                        444455555
  999900000                        999900000
  678901234                        678901234
0 ....+....                      0 ....+....
1 .........                      1 ..|||||..   ignoring this line and above
2 ...###...     should become    2 ..|###|..
3 ....#....                      3 ||||#||||
4 .#.....#.                      4 |#~~~~~#|
5 .#######.                      5 |#######|"
  "y=2, x=499..501\nx=500, y=3..3\nx=497, y=4..5\nx=503, y=4..5\ny=5, x=498..502")

(def OFFSET_RESTING_WATER "
  444555555                        444555555
  999000000                        999000000
  789012345                        789012345
0 ...+.....                      0 ...+.....
1 .........                      1 ...|.....   ignoring this line and above
2 .....#...     should become    2 ..|||#...
3 ...#.#...                      3 |||#~#...
4 .#..##...                      4 |#~~##...
5 .##....#.                      5 |##~~~~#.
6 ...#####.                      6 |..#####."
  "x=502, y=2..3\nx=500, y=3..3\ny=4, x=501..502\nx=498, y=4..5\ny=5, x=499..499\nx=504, y=5..6\ny=6, x=500..503")

(def PUZZLE_DATA (slurp "resources/advent_2018_clojure/day17_data.txt"))

(deftest part1-test
  (is (= 57 (part1 TEST_DATA)))
  (is (= 21 (part1 NESTED_BUCKETS)))
  (is (= 21 (part1 DOUBLE_DROP)))
  (is (= 3 (part1 FULLY_ENCLOSED)))
  (is (= 20 (part1 TWO_DRIPS_TO_SAME_TARGET)))
  (is (= 19 (part1 TWO_DRIPS_TO_SAME_TARGET_WITH_DIVIDER)))
  (is (= 50838 (part1 PUZZLE_DATA))))

(deftest run-water-test
  (are [input expected-points] (= (set expected-points)
                                  (->> input
                                       parse-board
                                       run-water
                                       (filter (comp water? second))
                                       (map first)
                                       set))
                               NESTED_BUCKETS (concat [[500 1] [500 2] [498 3] [499 3] [499 3]
                                                       [500 3] [501 3] [502 3] [503 3]
                                                       [504 3] [504 4] [504 5] [504 6] [504 7]]
                                                      [[498 4] [500 4] [502 4]
                                                       [498 5] [502 5]
                                                       [498 6] [499 6] [500 6] [501 6] [502 6]])
                               TOP_ROW [[499 1] [500 1] [501 1] [499 2] [501 2]]
                               OFFSET_RESTING_WATER [[500 1]
                                                     [499 2] [500 2] [501 2]
                                                     [497 3] [498 3] [499 3] [501 3] [497 4]
                                                     [499 4] [500 4]
                                                     [497 5] [500 5] [501 5] [502 5] [503 5]
                                                     [497 6]]))

(deftest part2-test
  (is (= 29 (part2 TEST_DATA)))
  (is (= 43039 (part2 PUZZLE_DATA))))