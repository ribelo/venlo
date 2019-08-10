(ns ribelo.venlo
  (:require
   [ribelo.wombat :as wb]
   [ribelo.wombat.utils :refer [comp-some]]))

()


(def scatter-plot
  ([dtfs {:keys {column begin-date end-date category-id
                 eans title width height min-y max-y agg
                 reg filetype}}]
   ()))
