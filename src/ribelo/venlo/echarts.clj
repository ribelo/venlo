(ns ribelo.venlo.echarts
  (:require
   [cheshire.core :as json]
   [cuerdas.core :as str]
   [hiccup.core :refer [html]]
   [taoensso.encore :as e]))

(defn- ?fn [?f elem]
  (if (fn? ?f) (?f elem) ?f))

(defn- reduce-opts [opts elem]
  (not-empty
   (reduce-kv (fn [acc k v]
                (assoc acc k (?fn v elem)))
              {}
              opts)))

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
  ([elem {:keys [color border-color border-width border-type shadow-blur
                 shadow-color shadow-offset-x shadow-offset-y opacity]
          :as   opts}]
   (when (not-empty opts)
     (not-empty
      (reduce-kv (fn [acc k v]
                   (assoc acc k (?fn v elem)))
                 {}
                 opts)))))

(defn ->area-style
  ([opts]
   (->item-style opts nil))
  ([elem {:keys [color origin shadow-blur shadow-color
                 shadow-offset-x shadow-offset-y opacity]
          :as   opts}]
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
   (e/assoc-some {:type      type}
                 :label      (->label label)
                 :item-style (->item-style item-style)
                 :area-style (->area-style area-style)
                 :data       data))
  ([{:keys [data type label item-style area-style] :as opts} coll]
   (e/assoc-some {:type      type}
                 :label      (->label label)
                 :item-style (->item-style item-style)
                 :area-style (->area-style area-style)
                 :data       (reduce-kv
                              (fn [acc k m]
                                (conj acc (->data (assoc m :k k) coll)))
                              []
                              data))))

(defn ->chart
  ([{:keys [series] :as opts}]
   (assoc opts :series (->series series)))
  ([{:keys [series] :as opts} coll]
   (assoc opts :series (->series series coll))))

(defn plot [{:keys [width height]
             :or   {width 900 height 400}
             :as   opts}]
  (let [id (str (java.util.UUID/randomUUID))
        code (format "var chart = echarts.init(document.getElementById(%s));
                      chart.setOption(%s)"
                     id (json/generate-string (-> opts (dissoc :width) (dissoc :height))
                                              {:key-fn str/camel}))]
    (html
     [:div [:div {:id id :style (format (str "width:%spx;"
                                             "height:%spx")
                                        width height)}]
      [:script code]])))
