module Fastlane
  module Actions
    class ActUploadInternalTrackAction < Action
      def self.run(params)
        upload_to_play_store(
          aab: params[:aab_path],
          track: "internal"
        )
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :aab_path,
            description: "Path to AAB",
            optional: false
          )
        ]
      end

      def self.description
        "Upload AAB to Google Play internal track"
      end

      def self.authors
        ["Slave"]
      end
    end
  end
end
