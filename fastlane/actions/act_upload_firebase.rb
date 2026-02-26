module Fastlane
  module Actions
    class ActUploadFirebaseAction < Action
      def self.run(params)
        firebase_app_distribution(
          app: params[:app_id],
          testers: params[:testers],
          release_notes: params[:release_notes],
          apk_path: params[:apk_path]
        )
      end

      def self.available_options
        [
          FastlaneCore::ConfigItem.new(
            key: :app_id,
            description: "Firebase App ID",
            optional: false
          ),
          FastlaneCore::ConfigItem.new(
            key: :apk_path,
            description: "Path to APK",
            optional: false
          ),
          FastlaneCore::ConfigItem.new(
            key: :testers,
            description: "Tester emails",
            optional: true
          ),
          FastlaneCore::ConfigItem.new(
            key: :release_notes,
            description: "Release notes",
            optional: true
          )
        ]
      end

      def self.description
        "Upload APK to Firebase App Distribution"
      end

      def self.authors
        ["Core Team"]
      end

      def self.is_supported?(platform)
        true
      end
    end
  end
end
