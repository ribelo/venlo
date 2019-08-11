(ns ribelo.venlo.echarts
  (:require
   [taoensso.encore :as e]))

(defn- ?fn [?f elem]
  (if (fn? ?f) (?f elem) ?f))

(defn- reduce-opts [opts elem]
  (not-empty
   (reduce-kv (fn [acc k v]
                (assoc acc k (?fn v elem)))
              {}
              opts)))

(defn ->x-axis
  [{:keys [kf]
    :as   opts} coll]
  (assoc opts :data (mapv kf coll)))

(defn ->y-axis
  [{:keys [kf]
    :as   opts} coll]
  (assoc opts :data (mapv kf coll)))

(defn ->label
  ([opts]
   (->label opts nil))
  ([{:keys [show position distance rotate offset formatter fmt
            color font-style font-weight font-family align
            vertical-align line-height background-color
            border-color border-radius padding shadow-color
            shadow-blur shadow-offset-x shadow-offset-y
            width height text-border-color text-border-width
            text-shadow-color text-shadow-blur text-shadow-offset-x
            text-shadow-offset-y rich]
     :as opts} elem]
   (when (not-empty opts)
     (when-let [label (reduce-opts opts elem)]
       {:label label}))))

(def ->rich ->label)

(defn ->item-style
  ([opts]
   (->item-style opts nil))
  ([{:keys [color border-color border-width border-type shadow-blur
            shadow-color shadow-offset-x shadow-offset-y opacity]
     :as   opts} elem]
   (when (not-empty opts)
     (not-empty
      (reduce-kv (fn [acc k v]
                   (assoc acc k (?fn v elem)))
                 {}
                 opts)))))

(defn ->area-style
  ([opts]
   (->item-style opts nil))
  ([{:keys [color origin shadow-blur shadow-color
            shadow-offset-x shadow-offset-y opacity]
     :as   opts} elem]
   (when (not-empty opts)
     (not-empty
      (reduce-kv (fn [acc k v]
                   (assoc acc k (?fn v elem)))
                 {}
                 opts)))))

(defn ->data [{:keys [k label item-style]} coll]
  (->> coll
       (into []
             (map (fn [elem]
                    (e/assoc-some {:value     (if k (get elem k) elem)}
                                  :label      (->label label elem)
                                  :item-style (->item-style item-style elem)))))))

(defn ->series
  ([{:keys [data type label item-style area-style] :as opts}]
   (e/assoc-some opts
                 :label      (->label label)
                 :item-style (->item-style item-style)
                 :area-style (->area-style area-style)
                 :data       data))
  ([{:keys [data type label item-style area-style x-axis y-axis] :as opts} coll]
   (e/assoc-some opts
                 :label      (->label label)
                 :item-style (->item-style item-style)
                 :area-style (->area-style area-style)
                 :x-axis     (->x-axis x-axis coll)
                 :y-axis     (->y-axis y-axis coll)
                 :data       (reduce-kv
                              (fn [acc k m]
                                (conj acc (->data (assoc m :k k) coll)))
                              []
                              data))))
